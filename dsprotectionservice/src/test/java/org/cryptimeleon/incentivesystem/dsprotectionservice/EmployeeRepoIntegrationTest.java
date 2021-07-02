package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentivesystem.dsprotectionservice.dummy.Employee;
import org.cryptimeleon.incentivesystem.dsprotectionservice.dummy.EmployeeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.logging.Logger;

@RunWith(SpringRunner.class)
@DataJpaTest
public class EmployeeRepoIntegrationTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    public void testSavingEmployee() {
        logger.info("Adding test employees");
        employeeRepository.save(new Employee(1, "Nagisa"));
        employeeRepository.save(new Employee(2, "Kaede"));

        logger.info("Retrieving added employees");
        List<Employee> employees = (List<Employee>) employeeRepository.findAll();

        Assertions.assertEquals(2, employees.size());
    }
}
