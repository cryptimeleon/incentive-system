package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@RestController
public class DsprotectionserviceController {
    @Autowired
    Environment env;

    /** dummy */
    @RequestMapping("/")
    public String hello()
    {
        return "It finally works. Great!";
    }

    /** dummy */
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

    /**
     * Returns an object representing the used database.
     * This object saves the location of the database endpoint as well as all necessary info for accessing the database.
     * @return object representing the used database
     */
    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("user"));
        dataSource.setPassword(env.getProperty("password"));
        return dataSource;
    }
}
