package org.cryptimeleon.incentivesystem.dsprotectionservice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles HTTP requests for double-spending protection database service.
 * Takes requests for adding transactions and double-spending IDs to the double spending database, as well as connections between them
 * (i.e. "transaction X produced token with dsid Y", "token with dsid Z was consumed by transaction W")
 * Request mapping is done via Spring Boot annotations.
 */
@RestController
public class DsprotectionController {
    private Logger logger = LoggerFactory.getLogger(DsprotectionController.class);

    DsprotectionService dsprotectionService;


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
     * Triggers execution of dbSync which synchronizes the passed transaction data (including dsid of spent token) into the database.
     * @param serializedTidRepr serialized representation of transaction ID
     * @param serializedDsidRepr serialized representation of double-spending protection ID
     * @param serializedDsTagRepr serialized representation of double-spending tag
     * @param spendAmount points spent in this transaction
     * @return success or error message as HTTP response
     */
    @PostMapping("/dbsync")
    public ResponseEntity<String> dbSync(
            @RequestHeader(value="tid") String serializedTidRepr,
            @RequestHeader(value="dsid") String serializedDsidRepr,
            @RequestHeader(value="dstag") String serializedDsTagRepr,
            @RequestHeader(value="k") String spendAmount
    ) {
        // trigger dbSync in Service class (which triggers it in IncentiveSystem instance)
        dsprotectionService.dbSync(serializedTidRepr, serializedDsidRepr, serializedDsTagRepr, spendAmount);

        // send response
        return new ResponseEntity<String>("Sent transaction data was recorded in database.", HttpStatus.OK);
    }
}