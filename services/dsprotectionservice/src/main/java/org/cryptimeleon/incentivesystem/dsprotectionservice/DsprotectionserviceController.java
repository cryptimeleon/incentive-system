package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.apache.coyote.Response;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.*;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
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
     * Simple heartbeating method that can be checked whether the double-spending protection service is still up and running.
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
    public ResponseEntity<String> addDsID(
            @RequestHeader(value = "dsid") String encodedDsID
    ) {
        return new ResponseEntity<String>("wip", HttpStatus.OK); // TODO implement this
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

    private Transaction convertTransactionEntry(TransactionEntry taEntry) {
        DsTagEntry taDsTagEntry = taEntry.getDsTagEntry();
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
}
