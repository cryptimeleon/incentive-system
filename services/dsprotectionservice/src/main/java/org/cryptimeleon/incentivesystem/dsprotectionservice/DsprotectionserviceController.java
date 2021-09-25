package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.apache.coyote.Response;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.*;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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

    // TODO: repo for userinfo

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
        // create database object
        TransactionEntry newEntry = new TransactionEntry(encodedTransaction, cryptoRepository.getPp());

        // add object to database
        transactionRepository.save(newEntry);

        // return status
        return new ResponseEntity<String>("Successfully added transaction.", HttpStatus.OK);
    }

    /**
     * Adds a new token (represented by its double-spending ID) to the database.
     * @param encodedDsID serialized representation of a group element
     * @return HTTP response object telling whether adding dsid worked
     */
    @PostMapping("/adddsid")
    public ResponseEntity<String> addTokenNode(
            @RequestHeader(value = "dsid") String encodedDsID
    ) {
        return new ResponseEntity<String>("wip", HttpStatus.OK); // TODO implement this
    }

    /**
     * Checks the transaction table for containment of a transaction with the passed identifying information.
     * The result is returned as a HTTP response object.
     */
    @GetMapping("/containsta")
    public ResponseEntity<Boolean> containsTransactionNode(
        @RequestHeader(value = "taidgamma") String serializedTransactionIdentifierRepr
    ) {
        // deserialize transaction identifier representation +  reconstruct identifier from representation
        JSONConverter jsonConverter = new JSONConverter();
        Representation taIdentifierRepresentation = jsonConverter.deserialize(serializedTransactionIdentifierRepr);
        TransactionIdentifier taIdentifier = new TransactionIdentifier(taIdentifierRepresentation, cryptoRepository.getPp());

        // check for containment of transaction
        boolean isContained = findTransactionWithTidGamma(taIdentifier.getTid(), taIdentifier.getGamma()) != null;

        // return response
        return new ResponseEntity<Boolean>(isContained, HttpStatus.OK);
    }

    /**
     * Retrieves transaction info for the transaction with the passed object ID from the database
     * @param id
     * @return
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
                taEntry.isValid(),
                taEntry.getTransactionID(),
                taEntry.getK(),
                new DoubleSpendingTag(
                        taDsTagEntry.getC0(),
                        taDsTagEntry.getC1(),
                        taDsTagEntry.getGamma(),
                        taDsTagEntry.getEskStarProv(),
                        taDsTagEntry.getCtrace0(),
                        taDsTagEntry.getCtrace1()
                )
        );
    }

    /**
     * Retrieves and returns the transaction entry for the transaction with the passed transaction ID and gamma if existent.
     */
    private TransactionEntry findTransactionWithTidGamma(Zn.ZnElement tid, Zn.ZnElement gamma) {
        // query all transaction entries from database
        ArrayList<TransactionEntry> taEntryList = (ArrayList<TransactionEntry>) transactionRepository.findAll();

        // look for one with fitting tid and gamma and return it
        for (TransactionEntry tae : taEntryList) {
            if(tae.getTransactionID().equals(tid)) { // less costly lookup in outer if-clause
                // retrieve corresponding double-spending tag entry
                DsTagEntry dste = doubleSpendingTagRepository.findById(tae.getDsTagEntryId()).get();
                if(dste.getGamma().equals(gamma)) {
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
    private DsIdEntry findDsid(GroupElement dsid) {
        // query all DSID entries from database
        ArrayList<DsIdEntry> dsidEntryList = (ArrayList<DsIdEntry>) dsidRepository.findAll();

        // look for one with fitting value and return it
        for(DsIdEntry dside : dsidEntryList) {
            if(dside.getDsid().equals(dsid)) {
                return dside;
            }
        }

        // drop-down if nothing found
        return null;
    }
}