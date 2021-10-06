package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.*;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @param encodedTransaction serialized representation of the transaction
     * @return HTTP response object telling whether adding transaction worked
     */
    @PostMapping("/addtransaction")
    public ResponseEntity<String> addTransaction(
            @RequestBody String encodedTransaction
    ) {
        // create transaction entry object
        TransactionEntry newEntry = new TransactionEntry(encodedTransaction, cryptoRepository.getPp());

        // add transaction entry object to database
        transactionRepository.save(newEntry);

        // TODO: add double spending tag to database

        // return status
        return new ResponseEntity<String>("Successfully added transaction.", HttpStatus.OK);
    }

    /**
     * Adds a new token (represented by its double-spending ID) to the database.
     * @param serializedDsidRepr serialized representation of a group element
     * @return HTTP response object telling whether adding dsid worked
     */
    @PostMapping("/adddsid")
    public ResponseEntity<String> addTokenNode(
            @RequestHeader(value = "dsid") String serializedDsidRepr
    ) {
        // deserialize dsid representation
        JSONConverter jsonConverter = new JSONConverter();
        Representation dsidRepresentation = jsonConverter.deserialize(serializedDsidRepr);
        GroupElement dsid = cryptoRepository.getPp().getBg().getG1().restoreElement(dsidRepresentation);

        // create dsid entry object
        DsIdEntry dsIdEntry = new DsIdEntry(Util.computeSerializedRepresentation(dsid));

        // add dsid entry object to database
        dsidRepository.save(dsIdEntry);

        // TODO: add user info object to database, adapt dsid entry class to also store user info entry

        // return status
        return new ResponseEntity<String>("Successfully added double-spending ID", HttpStatus.OK);
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
        boolean isContained = findTransactionEntryWithTidGamma(taIdentifier.getTid(), taIdentifier.getGamma()) != null;

        // return response
        return new ResponseEntity<Boolean>(isContained, HttpStatus.OK);
    }

    /**
     * Checks the double-spending ID table for containment of the passed dsid.
     * Result is returned as a HTTP response object.
     * @param serializedDsidRepr
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
        TransactionEntry tae = findTransactionEntryWithTidGamma(taIdentifier.getTid(), taIdentifier.getGamma());
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
     * @return
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
     * Retrieves and returns the transaction entry for the transaction with the passed transaction ID and gamma if existent.
     */
    private TransactionEntry findTransactionEntryWithTidGamma(Zn.ZnElement tid, Zn.ZnElement gamma) {
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