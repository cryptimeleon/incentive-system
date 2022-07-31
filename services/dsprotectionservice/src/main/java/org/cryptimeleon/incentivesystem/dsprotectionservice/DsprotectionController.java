package org.cryptimeleon.incentivesystem.dsprotectionservice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

/**
 * Handles HTTP requests for double-spending protection database service.
 * Takes requests for adding transactions and double-spending IDs to the double spending database, as well as connections between them
 * (i.e. "transaction X produced token with dsid Y", "token with dsid Z was consumed by transaction W")
 * Request mapping is done via Spring Boot annotations.
 */
@RestController
public class DsprotectionController {
    DsprotectionService dsprotectionService;
    private Logger logger = LoggerFactory.getLogger(DsprotectionController.class);

    /**
     * Standard constructor, tries to find required actual parameters from available beans.
     * beans = objects from classes that have an Autowired-constructor.
     */
    public DsprotectionController(DsprotectionService dsprotectionService) {
        this.dsprotectionService = dsprotectionService;
    }

    /**
     * Simple heartbeating method that can be used to check whether the double-spending protection service is still up and running.
     *
     * @return hard-coded standard response
     */
    @GetMapping("/")
    public ResponseEntity<String> heartbeat() {
        System.out.println("Someone did a heartbeat check!");
        return new ResponseEntity<>("Double-spending protection service up and running.", HttpStatus.OK);
    }


    /**
     * Triggers execution of dbSync which synchronizes the passed transaction data (including dsid of spent token) into the database.
     *
     * @param serializedTidRepr   serialized representation of transaction ID
     * @param serializedDsidRepr  serialized representation of double-spending protection ID
     * @param serializedDsTagRepr serialized representation of double-spending tag
     * @param userChoice          represents reward that user claimed with this promotion
     * @return success or error message as HTTP response
     */
    @PostMapping("/dbsync")
    public ResponseEntity<String> dbSync(
            @RequestHeader(value = "tid") String serializedTidRepr,
            @RequestHeader(value = "dsid") String serializedDsidRepr,
            @RequestHeader(value = "dstag") String serializedDsTagRepr,
            @RequestHeader(value = "promotion-id") BigInteger promotionId,
            @RequestHeader(value = "userchoice") String userChoice
    ) {
        // trigger dbSync in Service class (which triggers it in IncentiveSystem instance)
        dsprotectionService.dbSync(serializedTidRepr, serializedDsidRepr, serializedDsTagRepr, promotionId, userChoice);

        // send response
        return new ResponseEntity<>("Sent transaction data was recorded in database.", HttpStatus.OK);
    }

    /**
     * Endpoint for obtaining all transactions as a single JSON string that are currently contained in the database.
     * Transaction DTOs are stored as JSON objects in a JSON array automatically.
     *
     * @return response entity containing JSON object string
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        return new ResponseEntity<>(dsprotectionService.getAllTransactions(), HttpStatus.OK);
    }

    @PostMapping("/cleardb")
    public ResponseEntity<String> clearDatabase() {
        // trigger in service
        dsprotectionService.clearDatabase();

        // send response
        return new ResponseEntity<>("Cleared all tables, double-spending protection service still running.", HttpStatus.OK);
    }
}