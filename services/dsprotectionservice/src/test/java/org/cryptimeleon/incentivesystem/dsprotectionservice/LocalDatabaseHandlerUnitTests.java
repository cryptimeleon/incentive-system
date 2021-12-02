package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.util.ArrayList;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocalDatabaseHandlerUnitTests {
    private final Logger logger = LoggerFactory.getLogger(LocalDatabaseHandlerUnitTests.class);

    /**
     * Adds a transaction to the database. Verifies item count in database afterwards.
     */
    @Test
    public void addTransactionTest() {
        logger.info("Started transaction adding test.");

        logger.info("Setting up (implicit) incentive system for the test.");
        IncentivePublicParameters pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        LocalDatabaseHandler dbHandler = new LocalDatabaseHandler(pp);

        logger.info("Clear database."); // needed if this is not the first test in a sequence of tests
        dbHandler.clearDatabase();
        Assertions.assertTrue(
                ((ArrayList<TransactionEntry>)
                        dbHandler.transactionRepository.findAll()
                ).size()
                == 0
        );

        logger.info("Generating valid random transaction with associated double-spending tag.");
        var ta1 = Helper.generateTransaction(pp, true);
        logger.info("Adding generated transaction to the database.");
        dbHandler.addTransactionNode(ta1);

        logger.info("Generating invalid random transaction and associated double-spending tag.");
        var ta2 = Helper.generateTransaction(pp, false);
        logger.info("Adding generated transaction to the database.");
        dbHandler.addTransactionNode(ta2);

        logger.info("Checking whether double-spending database contains the correct number of entries.");
        Assertions.assertTrue(
                ((ArrayList<TransactionEntry>)
                        dbHandler.transactionRepository.findAll()
                ).size()
                        == 2
        );

        logger.info("Checking whether database contains both indivdual transactions.");
        boolean containsTa1 = dbHandler.containsTransactionNode(ta1.getTaIdentifier());
        Assertions.assertTrue(containsTa1);
        boolean containsTa2 = dbHandler.containsTransactionNode(ta2.getTaIdentifier());
        Assertions.assertTrue(containsTa2);

        logger.info("Transaction adding test completed.");
    }

    /**
     * Adds some random token nodes to the database and links random user info to them.
     */
    @Test
    public void addTokenTest() {

    }

    /**
     * Adds some transactions and tokens to the database, then connects them via edges and
     * checks whether edges are contained and
     * whether consumed tokens/consuming transactions are returned correctly when queried.
     */
    @Test
    public void edgeTest() {

    }

    /**
     * Adds a valid transaction and invalidates it twice.
     * Checks whether transaction is still invalid in the end.
     */
    @Test
    public void invalidateTaTest() {

    }
}
