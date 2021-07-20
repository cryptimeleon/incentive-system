package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentivesystem.dsprotectionservice.dummy.Employee;
import org.cryptimeleon.incentivesystem.dsprotectionservice.dummy.EmployeeRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.DsidRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
public class DsprotectionserviceController {
    @Autowired
    EmployeeRepository employeeRepository; // only for test

    @Autowired
    DsidRepository dsidRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @RequestMapping("/")
    public String hello()
    {
        return "We are in the process of constructing an awesome double-spending protection service here. Please be patient :)";
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
