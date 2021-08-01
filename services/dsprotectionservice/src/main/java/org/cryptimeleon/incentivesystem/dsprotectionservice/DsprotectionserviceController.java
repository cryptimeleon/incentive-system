package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsidRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/")
    public String hello()
    {
        return "We are in the process of constructing an awesome double-spending protection service here. Please be patient :)";
    }

    @RequestMapping(value = "/addta", params = {"encodedta"})
    public String addTransaction(
            @RequestParam(value = "encodedta") String encodedTransaction
    ) {
        return "wip";
    }

    @RequestMapping(value = "/adddsid", params = {"encodeddsid"})
    public String addDsID(
            @RequestParam(value = "id") long id,
            @RequestParam(value = "encodeddsid") String encodedDsID
    ) {
        return "wip";
    }

    @RequestMapping("/select")
    public String select()
    {
        return "wip";
    }
}
