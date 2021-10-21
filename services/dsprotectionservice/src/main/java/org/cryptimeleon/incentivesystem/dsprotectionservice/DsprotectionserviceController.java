package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.apache.catalina.User;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.*;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jooq.DslContextDependsOnPostProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Handles HTTP requests for double-spending protection database service.
 * Takes requests for adding transactions and double-spending IDs to the double spending database, as well as connections between them
 * (i.e. "transaction X produced token with dsid Y", "token with dsid Z was consumed by transaction W")
 * Request mapping is done via Spring Boot annotations.
 */
@RestController
public class DsprotectionserviceController {
    private Logger logger = LoggerFactory.getLogger(DsprotectionserviceController.class);

    @Autowired
    CryptoRepository cryptoRepository;

    @Autowired
    DsidRepository dsidRepository;

    @Autowired
    TransactionEntryRepository transactionRepository;

    @Autowired
    DsTagEntryRepository doubleSpendingTagRepository;

    @Autowired
    UserInfoRepository userInfoRepository;

    /**
     * Simple heartbeating method that can be used to check whether the double-spending protection service is still up and running.
     * @return hard-coded standard response
     */
    @GetMapping("/")
    public ResponseEntity<String> heartbeat()
    {
        return new ResponseEntity<String>("Hello from double-spending protection service!", HttpStatus.OK);
    }

    /**
     * Adds a new Spend transaction to the database.
     * @param serializedTaRepr serialized representation of the transaction
     * @return HTTP response object telling whether adding transaction worked
     */
    @PostMapping("/addtransaction")
    public ResponseEntity<String> addTransactionNode(
            @RequestHeader(value = "ta") String serializedTaRepr,
            @RequestBody String serializedDsTagRepr
    ) {
        logger.info("Received add transaction request");

        logger.info("Demarshalling data");

        // create transaction entry object
        TransactionEntry taEntry = new TransactionEntry(serializedTaRepr, cryptoRepository.getPp());

        // create double-spending tag entry object
        DsTagEntry dsTagEntry = new DsTagEntry(serializedDsTagRepr, cryptoRepository.getPp());

        logger.info("Saving and linking data");

        // add double spending tag entry to database
        doubleSpendingTagRepository.save(dsTagEntry);

        // link newly added dstag to transaction
        long dsTagEntryId = dsTagEntry.getId();
        taEntry.setDsTagEntryId(dsTagEntryId);

        // add transaction entry object (with linked dstag) to database
        transactionRepository.save(taEntry);

        logger.info("About to respond to add transaction request");

        // return status
        return new ResponseEntity<String>("Successfully added transaction.", HttpStatus.OK);
    }

    /**
     * Finds and returns the transaction that is identified by the passed transaction identifier.
     * @param serializedTaIdentifier serialized representation of the transaction identifier
     * @return serialized representation of transaction
     */
    @GetMapping("/gettransaction")
    public ResponseEntity<String> getTransactionNode(
            @RequestHeader(value = "taid") String serializedTaIdentifier
    ) {
        // deserialize and restore transaction
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(
                jsonConverter.deserialize(serializedTaIdentifier),
                cryptoRepository.getPp()
        );

        // retrieve the corresponding transaction
        TransactionEntry taEntry = this.findTransactionEntryWithTaIdentifier(taIdentifier);
        Transaction ta = this.convertTransactionEntry(taEntry);

        // marshall the transaction and return it
        return new ResponseEntity<String>(
                Util.computeSerializedRepresentation(ta),
                HttpStatus.OK
        );
    }

    /**
     * Adds a new token (represented by its double-spending ID) to the database.
     * @param serializedDsidRepr serialized representation of a double-spending ID
     * @return HTTP response object telling whether adding dsid worked
     */
    @PostMapping("/adddsid")
    public ResponseEntity<String> addTokenNode(
            @RequestHeader(value = "dsid") String serializedDsidRepr
    ) {
        // create dsid entry object
        DsIdEntry dsIdEntry = new DsIdEntry(serializedDsidRepr);

        // add dsid entry object to database
        dsidRepository.save(dsIdEntry);

        // return status
        return new ResponseEntity<String>("Successfully added double-spending ID", HttpStatus.OK);
    }

    /**
     * Makes an edge from the passed transaction to the passed double-spending ID, if nodes for both exist.
     * Semantics of this edge: the passed transaction produced the token with the passed double-spending ID
     * @param serializedTaIdRepr serialized representation of transaction
     * @param serializedDsIdRepr serialized representation of double-spending ID
     * @return success or failure report (HTTP response)
     */
    @PostMapping("/addtatokenedge")
    public ResponseEntity<String> addTransactionTokenEdge(
        @RequestHeader(value = "taid") String serializedTaIdRepr,
        @RequestHeader(value = "dsid") String serializedDsIdRepr
    ) {
        // deserialize transaction identifier and double-spending ID
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(
                jsonConverter.deserialize(serializedTaIdRepr),
                cryptoRepository.getPp()
        );
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(
                jsonConverter.deserialize(serializedDsIdRepr)
        );

        // retrieve entries for respective transaction and dsID
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taIdentifier);
        DsIdEntry dsIdEntry = findDsidEntry(dsid);

        // make edge (i.e. update field of ta entry that holds ID of produced token's dsid) if both nodes exist
        if(taEntry != null && dsIdEntry != null) {
            transactionRepository.delete(taEntry);
            taEntry.setProcucedDsidEntryId(dsIdEntry.getId());
            transactionRepository.save(taEntry);

            return new ResponseEntity<String>("Successfully added transaction->token edge.", HttpStatus.OK);
        }
        // send error report if either transaction or dsid was not found in the database
        else {
            if (taEntry == null) {
                return new ResponseEntity<String>("No transaction corresponding to the queried identifier was found in database.", HttpStatus.OK);
            }
            if (dsIdEntry == null) {
                return new ResponseEntity<String>("Queried double-spending ID not found in database.", HttpStatus.OK);
            }
            return new ResponseEntity<String>("An unknown error occurred. No write operations performed on database.", HttpStatus.OK);
        }
    }

    /**
     * Makes an edge from the passed double-spending ID to the passed transaction (if both nodes exist).
     * Semantics of this edge: the token with the passed double-spending ID was consumed in the passed transaction
     * @param serializedDsIdRepr serialized representation of double-spending ID
     * @param serializedTaIdRepr serialized representation of transaction
     * @return success or failure report (HTTP response)
     */
    @PostMapping("/addtokentaedge")
    public ResponseEntity<String> addTokenTransactionEdge(
        @RequestHeader(value = "dsid") String serializedDsIdRepr,
        @RequestHeader(value = "taid") String serializedTaIdRepr
    ) {
        // deserialize transaction identifier and double-spending ID
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(
                jsonConverter.deserialize(serializedTaIdRepr),
                cryptoRepository.getPp()
        );
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(
                jsonConverter.deserialize(serializedDsIdRepr)
        );

        // retrieve entries for respective transaction and dsID
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taIdentifier);
        DsIdEntry dsIdEntry = findDsidEntry(dsid);

        // make edge (i.e. update field of transaction entry that holds ID of consumed dsid's entry) if both nodes exist
        if(taEntry != null && dsIdEntry != null) {
            transactionRepository.delete(taEntry);
            taEntry.setConsumedDsidEntryId(dsIdEntry.getId());
            transactionRepository.save(taEntry);

            return new ResponseEntity<String>("Successfully added token->transaction edge.", HttpStatus.OK);
        }
        // send error report if either transaction or dsid was not found in the database
        else {
            if (taEntry == null) {
                return new ResponseEntity<String>("No transaction corresponding to the queried identifier was found in database.", HttpStatus.OK);
            }
            if (dsIdEntry == null) {
                return new ResponseEntity<String>("Queried double-spending ID not found in database.", HttpStatus.OK);
            }
            return new ResponseEntity<String>("An unknown error occurred. No write operations performed on database.", HttpStatus.OK);
        }
    }

    /**
     * Checks the transaction table for containment of a transaction with the passed identifying information.
     * The result is returned as a HTTP response object.
     * @param serializedTransactionIdentifierRepr serialized representation of transaction identifier
     */
    @GetMapping("/containsta")
    public ResponseEntity<Boolean> containsTransactionNode(
        @RequestHeader(value = "taidgamma") String serializedTransactionIdentifierRepr
    ) {
        // deserialize transaction identifier representation + reconstruct identifier from representation
        JSONConverter jsonConverter = new JSONConverter();
        Representation taIdentifierRepresentation = jsonConverter.deserialize(serializedTransactionIdentifierRepr);
        TransactionIdentifier taIdentifier = new TransactionIdentifier(taIdentifierRepresentation, cryptoRepository.getPp());

        // check for containment of transaction
        boolean isContained = findTransactionEntryWithTaIdentifier(taIdentifier) != null;

        // return response
        return new ResponseEntity<Boolean>(isContained, HttpStatus.OK);
    }

    /**
     * Checks the double-spending ID table for containment of the passed dsid.
     * Result is returned as a HTTP response object.
     * @param serializedDsidRepr serialized dsid representation
     */
    @GetMapping("/containsdsid")
    public ResponseEntity<Boolean> containsTokenNode(
        @RequestHeader(value= "dsid") String serializedDsidRepr
    ) {
        // deserialize dsid representation + reconstruct dsid from representation
        JSONConverter jsonConverter = new JSONConverter();
        Representation dsidRepresentation = jsonConverter.deserialize(serializedDsidRepr);
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(dsidRepresentation);

        // check for containment of dsid
        boolean isContained = findDsidEntry(dsid) != null;

        // return response
        return new ResponseEntity<Boolean>(isContained, HttpStatus.OK);
    }

    /**
     * Checks the database for a connection from passed transaction to token with passed double-spending ID.
     * So this method checks whether said transaction produced said token.
     * Result is returned as HTTP response object.
     * @param serializedTaIdRepr serialized transaction identifier representation
     * @param serializedDsIdRepr serialized double-spending ID representation
     * @return HTTP response with result
     */
    @GetMapping(name = "/containstatokenedge")
    public ResponseEntity<Boolean> containsTransactionTokenEdge(
            @RequestHeader("taid") String serializedTaIdRepr,
            @RequestHeader("dsid") String serializedDsIdRepr
    ) {
        // deserialize transaction identifier and double-spending ID
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(
                jsonConverter.deserialize(serializedTaIdRepr),
                cryptoRepository.getPp()
        );
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(
                jsonConverter.deserialize(serializedDsIdRepr)
        );

        // retrieve entries for respective transaction and dsID
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taIdentifier);
        DsIdEntry dsIdEntry = findDsidEntry(dsid);

        // check for existence of edge
        boolean edgeExists = taEntry != null &&
                dsIdEntry != null &&
                taEntry.getProcucedDsidEntryId() == dsIdEntry.getId();

        return new ResponseEntity<Boolean>(edgeExists, HttpStatus.OK);
    }

    /**
     * Checks the database for a connection from token with passed double-spending ID to passed transaction.
     * So this method checks whether said token was consumed in said transaction.
     * Result is returned as HTTP response object.
     * @param serializedTaIdRepr serialized transaction identifier representation
     * @param serializedDsIdRepr serialized double-spending ID representation
     * @return HTTP response with result
     */
    @GetMapping("/containstokentaedge")
    public ResponseEntity<Boolean> containsTokenTransactionEdge(
            @RequestHeader("dsid") String serializedDsIdRepr,
            @RequestHeader("taid") String serializedTaIdRepr
    ) {
        // deserialize transaction identifier and double-spending ID
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(
                jsonConverter.deserialize(serializedTaIdRepr),
                cryptoRepository.getPp()
        );
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(
                jsonConverter.deserialize(serializedDsIdRepr)
        );

        // retrieve entries for respective transaction and dsID
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taIdentifier);
        DsIdEntry dsIdEntry = findDsidEntry(dsid);

        // check for existence of edge
        boolean edgeExists = taEntry != null &&
                dsIdEntry != null &&
                dsIdEntry.getId() == taEntry.getConsumedDsidEntryId();

        return new ResponseEntity<Boolean>(edgeExists, HttpStatus.OK);
    }

    /**
     * Invalidates the transaction with the passed identifier by modifying the respective transaction entry, if existent.
     * @return success or failure HTTP response
     */
    @PostMapping("/invalidateta")
    public ResponseEntity<String> invalidateTransaction(
            @RequestHeader(value = "taidgamma") String serializedTransactionIdentifierRepr
    ) {
        // deserialize transaction identifier representation + reconstruct identifier from representation
        JSONConverter jsonConverter = new JSONConverter();
        Representation taIdentifierRepresentation = jsonConverter.deserialize(serializedTransactionIdentifierRepr);
        TransactionIdentifier taIdentifier = new TransactionIdentifier(taIdentifierRepresentation, cryptoRepository.getPp());

        // if existent: update respective transaction entry (retrieve, then delete from DB; add modified entry)
        TransactionEntry tae = findTransactionEntryWithTaIdentifier(taIdentifier);
        if(tae != null) {
            transactionRepository.delete(tae);
            tae.invalidate();
            transactionRepository.save(tae);

            // return (hard-coded) success response
            return new ResponseEntity<String>("Successfully deleted transaction.", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<String>("No transaction with passed identifier found.", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Adds the passed user info entry to the database and links it to the entry for the passed dsid.
     * @return success or failure HTTP response
     */
    @PostMapping("/adduserinfo")
    public ResponseEntity<String> addAndLinkUserInfo(
            @RequestBody String serializedUserInfoRepr,
            @RequestHeader(value="dsid") String serializedDsidRepr
    ) {
        // create user info entry object
        UserInfoEntry uie = new UserInfoEntry(serializedUserInfoRepr, cryptoRepository.getPp());

        // add user info entry object to the database
        userInfoRepository.save(uie);

        // deserialize dsid
        JSONConverter jsonConverter = new JSONConverter();
        Representation dsidRepr = jsonConverter.deserialize(serializedDsidRepr);
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(dsidRepr);

        // update dsid entry if existent
        long uInfoEntryId = uie.getId();
        DsIdEntry dside = findDsidEntry(dsid);
        if(dside != null) {
            dsidRepository.delete(dside);
            dside.setAssociatedUserInfoId(uInfoEntryId);
            dsidRepository.save(dside);

            return new ResponseEntity<String>("Successfully added user info and linked it to a double-spending ID", HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<String>("User info added, but could not link it to a double-spending ID", HttpStatus.OK);
        }
    }

    /**
     * Retrieves the (anonymized) user info associated to the token identified by the passed dsid.
     * @param serializedDsidRepr serialized representation of the double-spending ID in question
     * @return success or failure HTTP response containing the queried user info (if existent)
     */
    @GetMapping("/getuserinfo")
    public ResponseEntity<String> getUserInfo(
            @RequestHeader(value = "dsid") String serializedDsidRepr
    ) {
        // deserialize and retrieve passed dsid
        JSONConverter jsonConverter = new JSONConverter();
        Representation dsidRepr = jsonConverter.deserialize(serializedDsidRepr);
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(dsidRepr);

        // query user info from database
        DsIdEntry dside = findDsidEntry(dsid);
        long uieId = dside.getAssociatedUserInfoId();
        UserInfoEntry uie = userInfoRepository.findById(uieId).get();

        // convert user info entry to user info
        UserInfo uInfo = convertUserInfoEntry(uie);

        // return response
        return new ResponseEntity<String>(Util.computeSerializedRepresentation(uInfo), HttpStatus.OK);
    }

    /**
     * Retrieves all transactions that consumed the token with the passed dsid.
     * @param serializedDsidRepr serialized double-spending ID representation
     * @return HTTP response containing a list of serialized transaction representations
     */
    @GetMapping("/getconsumingta")
    public ResponseEntity<ArrayList<String>> getConsumingTransactions(
            @RequestHeader(value = "dsid") String serializedDsidRepr
    ) {
        // deserialize and retrieve passed dsid
        JSONConverter jsonConverter = new JSONConverter();
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(
                jsonConverter.deserialize(serializedDsidRepr)
        );

        // query respective entries
        ArrayList<TransactionEntry> taeList = getConsumingTransactionEntries(
                findDsidEntry(dsid).getId()
        );

        // marshall entries for transport
        ArrayList<String> serializedTaReprList = new ArrayList<String>();
        taeList.forEach(tae -> {
            Transaction ta = convertTransactionEntry(tae);
            String serializedTaRepr = Util.computeSerializedRepresentation(ta);
            serializedTaReprList.add(serializedTaRepr);
        });

        return new ResponseEntity<ArrayList<String>>(serializedTaReprList, HttpStatus.OK);
    }

    /**
     * Retrieves the double-spending ID of the token that was consumed in the transaction with the passed identifier.
     * @param serializedTaIdentRepr serialized transaction identifier representation
     * @return HTTP response containing serialized double-spending ID representation
     */
    @GetMapping("/getconsumedtoken")
    public ResponseEntity<String> getConsumedTokenDsid(
            @RequestHeader(value = "taid") String serializedTaIdentRepr
    ) {
        // deserialize and restore transaction identifier
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdent = new TransactionIdentifier(
                jsonConverter.deserialize(serializedTaIdentRepr),
                cryptoRepository.getPp()
        );

        // find transaction entry
        TransactionEntry taEntry = findTransactionEntryWithTaIdentifier(taIdent);

        // find dsid entry of consumed token's double-spending ID
        DsIdEntry consumedDsidEntry = dsidRepository.findById(taEntry.getConsumedDsidEntryId()).get();

        // return serialized dsid representation
        return new ResponseEntity<String>(
                consumedDsidEntry.getSerializedDsidRepr(),
                HttpStatus.OK
        );
    }









    /**
     * helper methods
     */




    /**
     * Converts a transaction database entry to a normal (crypto) transaction.
     * The original object is not changed.
     * @param taEntry original transaction entry
     * @return transaction object
     */
    private Transaction convertTransactionEntry(TransactionEntry taEntry) {
        DsTagEntry taDsTagEntry = doubleSpendingTagRepository.findById(taEntry.getDsTagEntryId()).get();
        return new Transaction(
                this.cryptoRepository.getPp(),
                taEntry.isValid(),
                taEntry.getSerializedTransactionIDRepr(),
                taEntry.getK(),
                taDsTagEntry.getSerializedC0Repr(),
                taDsTagEntry.getSerializedC1Repr(),
                taDsTagEntry.getSerializedGammaRepr(),
                taDsTagEntry.getSerializedEskStarProvRepr(),
                taDsTagEntry.getSerializedCTrace0Repr(),
                taDsTagEntry.getSerializedCTrace1Repr()
        );
    }

    /**
     * Converts a user info database entry object to a normal (crypto) user info object.
     * The original object is not changed.
     * @param uiEntry original user info entry
     * @return user info object
     */
    private UserInfo convertUserInfoEntry(UserInfoEntry uiEntry) {
        // create JSON converter and shorthand for the ring Zn used in the system
        JSONConverter jsonConverter = new JSONConverter();
        Zn usedZn = cryptoRepository.getPp().getBg().getZn();

        // restore fields from representations
        UserPublicKey upk = new UserPublicKey(
                jsonConverter.deserialize(uiEntry.getSerializedUpkRepr()),
                cryptoRepository.getPp().getBg().getG1()
        );
        Zn.ZnElement dsBlame = usedZn.restoreElement(
                jsonConverter.deserialize(uiEntry.getSerializedDsBlameRepr())
        );
        Zn.ZnElement dsTrace = usedZn.restoreElement(
                jsonConverter.deserialize(uiEntry.getSerializedDsTraceRepr())
        );

        return new UserInfo(upk, dsBlame, dsTrace);
    }

    /**
     * Retrieves and returns the transaction entry for the transaction with the passed transaction ID and gamma if existent.
     */
    private TransactionEntry findTransactionEntryWithTaIdentifier(TransactionIdentifier taIdentifier) {
        Zn.ZnElement tid = taIdentifier.getTid();
        Zn.ZnElement gamma = taIdentifier.getGamma();

        // query all transaction entries from database
        ArrayList<TransactionEntry> taEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();


        JSONConverter jsonConverter = new JSONConverter(); // need a JSON converter for deserializing tids and gammas of transaction entries (required for comparison)
        Zn usedZn = cryptoRepository.getPp().getBg().getZn();// store used ZN to shorten code

        // look for one with fitting tid and gamma and return it
        for (TransactionEntry tae : taEntryList) {
            // deserialize tid of currently considered transaction entry
            Zn.ZnElement taeTid = usedZn.restoreElement(jsonConverter.deserialize(tae.getSerializedTransactionIDRepr()));

            if(taeTid.equals(tid)) { // less costly lookup in outer if-clause
                // retrieve corresponding double-spending tag entry
                DsTagEntry dste = doubleSpendingTagRepository.findById(tae.getDsTagEntryId()).get();

                // deserialize gamma of currently considered transaction entry
                Zn.ZnElement dsteGamma = usedZn.restoreElement(jsonConverter.deserialize(dste.getSerializedGammaRepr()));

                if(dsteGamma.equals(gamma)) {
                    return tae;
                }
            }
        }

        // drop-down if nothing found
        return null;
    }

    /**
     * Retrieves and returns the double-spending ID entry for the double-spending ID with the passed value (if existent).
     */
    private DsIdEntry findDsidEntry(GroupElement dsid) {
        // query all DSID entries from database
        ArrayList<DsIdEntry> dsidEntryList = (ArrayList<DsIdEntry>) dsidRepository.findAll();

        // create a JSON converter + store first group from used bilinear group (to shorten code)
        JSONConverter jsonConverter = new JSONConverter();
        Group groupG1 = cryptoRepository.getPp().getBg().getG1();

        // look for one with fitting value and return it
        for(DsIdEntry dside : dsidEntryList) {
            // deserialize dsid of currently considered dsid entry
            GroupElement dsideDsid = groupG1.restoreElement(jsonConverter.deserialize(dside.getSerializedDsidRepr()));

            if(dsideDsid.equals(dsid)) {
                return dside;
            }
        }

        // drop-down if nothing found
        return null;
    }

    /**
     * Retrieves and returns all transactions that have consumed a Dsid whose corresponding database entry has the passed ID.
     * @param dsidEntryId database entry ID
     * @return list of transactions
     */
    public ArrayList<TransactionEntry> getConsumingTransactionEntries(long dsidEntryId) {
        ArrayList<TransactionEntry> resultList = new ArrayList<TransactionEntry>();

        // query all transaction entries from database
        ArrayList<TransactionEntry> transactionEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();

        // filter by consumed dsid entry ID
        for(TransactionEntry tae : transactionEntryList) {
            if(tae.getConsumedDsidEntryId() == dsidEntryId) {
                resultList.add(tae);
            }
        }

        return resultList;
    }


    /**
     * Retrieves transaction info for the transaction entry with the passed object ID from the database
     * @param id
     * @return transaction object
     */
    @GetMapping("/retrieveta")
    public ResponseEntity<String> retrieveTransaction(
            @RequestHeader(value = "id") long id
    )
    {
        // query transaction entry by ID
        TransactionEntry taEntry = null;
        try {
            taEntry = transactionRepository.findById(id).get();
        }
        // return empty response with error code if not found
        catch (NoSuchElementException e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        // convert transaction entry object (dsprotectionservice) to transaction object (crypto)
        Transaction ta = convertTransactionEntry(taEntry);

        // serialize transaction as string
        JSONConverter jsonConverter = new JSONConverter();
        String serializedTa = jsonConverter.serialize(ta.getRepresentation());

        // return transaction and status
        return new ResponseEntity<String>(serializedTa, HttpStatus.OK);
    }

    /**
     * Retrieves transaction info for all transactions in the database.
     * @return list of transaction objects
     */
    @GetMapping("retrieveallta")
    public ResponseEntity<ArrayList<String>> retrieveAllTransactions() {
        // query all transaction entries from database
        ArrayList<TransactionEntry> taEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();

        // convert all of them to (crypto) transaction objects whose serialized representations aggregated in a list
        ArrayList<String> serializedTaRepresentationsList = new ArrayList<String>();
        JSONConverter jsonConverter = new JSONConverter();
        taEntryList.forEach(taEntry -> {
            Transaction ta = convertTransactionEntry(taEntry);
            Representation taRepr = ta.getRepresentation();
            String serializedTaRepr = jsonConverter.serialize(taRepr);
            serializedTaRepresentationsList.add(serializedTaRepr);
        });

        // return response
        return new ResponseEntity<ArrayList<String>>(serializedTaRepresentationsList, HttpStatus.OK);
    }
}