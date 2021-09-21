package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DoubleSpendingTagRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsidRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO: implement a proper request mapping as in IssueController, CreditController, ...

/**
 * Handles HTTP requests for double-spending protection database service.
 * Takes requests for adding transactions and double-spending IDs to the double spending database, as well as connections between them
 * (i.e. "transaction X produced token with dsid Y", "token with dsid Z was consumed by transaction W")
 * Request mapping is done via Spring Boot annotations.
 */
@RestController
public class DsprotectionserviceController {
    @Autowired
    DsidRepository dsidRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    DoubleSpendingTagRepository doubleSpendingTagRepository;

    // TODO: repos for userinfo

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
        return new ResponseEntity<String>("wip", HttpStatus.OK);
    }

    /**
     * Adds a new token (represented by its double-spending ID) to the database.
     * @param id object id of the double spending ID inside the database (primary key)
     * @param encodedDsID serialized representation of a group element
     * @return HTTP response object telling whether adding dsid worked
     */
    @PostMapping("/adddsid")
    public ResponseEntity<String> addDsID(
            @RequestHeader(value = "object-id") long id,
            @RequestHeader(value = "dsid") String encodedDsID
    ) {
        return new ResponseEntity<String>("wip", HttpStatus.OK);
    }

    // TODO: create method signature with proper parameters
    @GetMapping("/select")
    public ResponseEntity<String> select()
    {
        return new ResponseEntity<String>("wip", HttpStatus.OK);
    }
}
