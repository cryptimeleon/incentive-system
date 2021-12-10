package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockDsTagEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockDsidEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockTransactionEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockUserInfoEntryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the DB Sync algorithm using dummy transactions.
 */
public class DbSyncIntegrationTest {
    private Logger logger = LoggerFactory.getLogger(DbSyncIntegrationTest.class);

    /**
     * Syncing two transactions into the DB that spend different tokens.
     */
    @Test
    void insertHonestTransactionTest() {
        logger.info("Starting honest transactions test.");

        logger.info("Setup incentive system and database handler for the test.");
        var pp = IncentiveSystem.setup(256, Setup.BilinearGroupChoice.Debug);
        var incSys = new IncentiveSystem(pp);
        var dbHandler = new LocalDatabaseHandler(
                incSys.getPp(),
                new MockDsidEntryRepository(),
                new MockTransactionEntryRepository(),
                new MockDsTagEntryRepository(),
                new MockUserInfoEntryRepository()
        );

        logger.info("Generating random valid transactions and dsids.");
        var ta1 = Helper.generateTransaction(incSys.pp, true);
        var dsid1 = pp.getBg().getG1().getUniformlyRandomElement();
        var ta2 = Helper.generateTransaction(incSys.pp, true);
        var dsid2 = pp.getBg().getG1().getUniformlyRandomElement();

        logger.info("Adding transactions to database.");
        incSys.dbSync(
                ta1.getTransactionID(),
                dsid1,
                ta1.getDsTag(),
                ta1.getK(),
                dbHandler
        );
        incSys.dbSync(
                ta2.getTransactionID(),
                dsid2,
                ta2.getDsTag(),
                ta2.getK(),
                dbHandler
        );

        logger.info("Checking containment of nodes and edges, invalidity of transactions.");

        // nodes
        Assertions.assertTrue(
                dbHandler.containsTransactionNode(ta1.getTaIdentifier())
        );
        Assertions.assertTrue(
                dbHandler.containsTransactionNode(ta2.getTaIdentifier())
        );
        Assertions.assertTrue(
                dbHandler.containsTokenNode(dsid1)
        );
        Assertions.assertTrue(
                dbHandler.containsTokenNode(dsid2)
        );

        // edges
        Assertions.assertTrue(
                dbHandler.containsTokenTransactionEdge(dsid1, ta1.getTaIdentifier())
        );
        Assertions.assertTrue(
                !dbHandler.containsTokenTransactionEdge(dsid1, ta2.getTaIdentifier())
        );
        Assertions.assertTrue(
                !dbHandler.containsTokenTransactionEdge(dsid2, ta1.getTaIdentifier())
        );
        Assertions.assertTrue(
                dbHandler.containsTokenTransactionEdge(dsid2, ta2.getTaIdentifier())
        );

        logger.info("Completed honest transactions test.");
    }

    /**
     * Syncing two transactions into the DB that spend the same token.
     */
    @Test
    void doubleSpendingTest() {

    }
}
