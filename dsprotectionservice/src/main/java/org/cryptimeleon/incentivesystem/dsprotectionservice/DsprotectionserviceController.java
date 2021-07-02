package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentivesystem.dsprotectionservice.dummy.Employee;
import org.cryptimeleon.incentivesystem.dsprotectionservice.dummy.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

@RestController
public class DsprotectionserviceController {
    @Autowired
    EmployeeRepository employeeRepository;

    @RequestMapping("/")
    public String hello()
    {
        return "We are in the process of constructing an awesome double-spending protection service here. Please be patient :)";
    }

    @RequestMapping(value = "/save", params = {"id", "name"})
    public String save(
            @RequestParam(value = "id") long id,
            @RequestParam(value = "name") String name
    ) {
        employeeRepository.save(new Employee(id, name));
        return "Successfully inserted new employee!";
    }

    @RequestMapping("/select")
    public String select()
    {
        List<Employee> employees = (List<Employee>) employeeRepository.findAll();
        return employees.toString();
    }
}
