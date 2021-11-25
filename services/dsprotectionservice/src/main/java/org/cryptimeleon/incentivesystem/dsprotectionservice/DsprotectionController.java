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

    //TODO:annotation needed?
    LocalDatabaseHandler localDbHandler;

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
     * Clears all tables of the double-spending database.
     * Needed for test runs where different test scenarios are created without restarting the double-spending protection service after each test.
     * @return HTTP response body content
     */
    @PostMapping("/cleardb")
    public ResponseEntity<String> clearDatabase() {
        localDbHandler.clearDatabase();

        return new ResponseEntity<String>("All tables cleared. Double-spending protection service still running.", HttpStatus.OK);
    }
}