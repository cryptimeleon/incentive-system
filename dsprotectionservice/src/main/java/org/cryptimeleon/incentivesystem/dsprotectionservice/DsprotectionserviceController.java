package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.ResultSet;

@RestController
public class DsprotectionserviceController {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/")
    public String hello()
    {
        return "It finally works. Great!";
    }

    @RequestMapping("/construction")
    public String construction()
    {
        return "We are in the process of constructing an awesome double-spending protection service here. Please be patient :)";
    }

    @RequestMapping("/select")
    public String select()
    {
        // TODO: how to execute query and obtain result as ResultSet or anything else than can be casted into a String?

        ResultSet results = null;
        return results.toString();
    }
}
