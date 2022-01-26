package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockDsTagEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockDsidEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockTransactionEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockUserInfoEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Tests for the DB Sync algorithm using dummy transactions.
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

        logger.info("Clear database."); // needed if this is not the first test in a sequence of tests
        dbHandler.clearDatabase();
        Assertions.assertTrue(
                ((ArrayList<TransactionEntry>)
                        dbHandler.transactionRepository.findAll()
                ).size() == 0
        );

        logger.info("Generating random valid transactions and dsids.");
        var ta1 = Helper.generateRandomTransaction(incSys.pp, true);
        var dsid1 = pp.getBg().getG1().getUniformlyRandomElement();
        var ta2 = Helper.generateRandomTransaction(incSys.pp, true);
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
     * Syncing transactions into the DB, some of which spend the same token.
     * This should be detected as a double-spending attempt and result in cascading invalidations of transactions.
     * We use the graph from section 6 of the 2019 Updatable Anonymous Credentials paper for testing.
     *
     * Double-spending IDs dsid4 and dsid5 are computed during the double-spending detection.
     * This comes from the fact that until double-spending behaviour is detected,
     * it is actually not interesting which transaction produced which token.
     */
    @Test
    void cascadingInvalidationsTest() {
        logger.info("Starting cascading invalidations test.");

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

        logger.info("Clear database."); // needed if this is not the first test in a sequence of tests
        dbHandler.clearDatabase();
        Assertions.assertTrue(
                ((ArrayList<TransactionEntry>)
                        dbHandler.transactionRepository.findAll()
                ).size() == 0
        );

        logger.info("Generating random valid transactions and dsids.");
        var usedG1 = incSys.getPp().getBg().getG1();
        var t1 = Helper.generateRandomTransaction(incSys.getPp(), true);
        var t1Prime = Helper.generateRandomTransaction(incSys.getPp(), true);
        var t2 = Helper.generateRandomTransaction(incSys.getPp(), true);
        var t2Prime = Helper.generateRandomTransaction(incSys.getPp(), true);
        var t3 = Helper.generateRandomTransaction(incSys.getPp(), true);
        var dsid1 = usedG1.getUniformlyRandomElement();
        var dsid2 = usedG1.getUniformlyRandomElement(); // we want dsid2 to be the successor of t1Prime, as in the graph from the paper
        var dsid3 = usedG1.getUniformlyRandomElement(); // we want dsid3 to be the successor of t2, as in the graph from the paper

        logger.info("Syncing transactions and dsids to database.");
        logger.info("Syncing some honest spend transactions.");
        logger.info("Syncing t2Prime which spent dsid2");
        incSys.dbSync( // t2Prime and dsid2
                t2Prime.getTaIdentifier().getTid(),
                dsid2,
                t2Prime.getDsTag(),
                t2Prime.getK(),
                dbHandler
        );
        logger.info("Syncing t1 which spent dsid1");
        incSys.dbSync( // t1 and dsid1
                t1.getTaIdentifier().getTid(),
                dsid1,
                t1.getDsTag(),
                t1.getK(),
                dbHandler
        );
        logger.info("Syncing t3 which spent dsid3");
        incSys.dbSync( // t3 and dsid3
                t3.getTaIdentifier().getTid(),
                dsid3,
                t3.getDsTag(),
                t3.getK(),
                dbHandler
        );
        logger.info("Done syncing honest transactions.");

        logger.info("Checking integrity conditions.");

        Assertions.assertEquals(3, dbHandler.getTransactionCount(), 3);
        logger.info("All honest transactions contained.");
        Assertions.assertEquals(3, dbHandler.getTokenCount());
        logger.info("All corresponding double-spending IDs contained.");
        Assertions.assertEquals(3, dbHandler.getDsTagCount());
        logger.info("All double-spending tags for honest transactions contained.");
        Assertions.assertEquals(0, dbHandler.getUserInfoCount());
        logger.info("No user info contained yet.");
        logger.info("Note: this is due to the fact that user info is only computed when double-spending attempts are detected.");

        logger.info("Done checking integrity conditions.");

        logger.info("Syncing t1Prime which spent dsid1");
        incSys.dbSync( // t1Prime and dsid1
                t1Prime.getTaIdentifier().getTid(),
                dsid1,
                t1Prime.getDsTag(),
                t1.getK(),
                dbHandler
        );
        logger.info("Syncing t2 which spent dsid2");
        incSys.dbSync( // t2 and dsid2
                t2.getTaIdentifier().getTid(),
                dsid2,
                t2.getDsTag(),
                t2Prime.getK(),
                dbHandler
        );
        logger.info("Done syncing transactions and dsids to database.");

        logger.info("Checking whether double spending was correctly detected.");

        logger.info("Checking token count.");
        Assertions.assertEquals(dbHandler.getTokenCount(), 5);

        logger.info("Checking validity of transactions.");
        var retrievedT1 = dbHandler.getTransactionNode(t1.getTaIdentifier());
        var retrievedT1Prime = dbHandler.getTransactionNode(t1Prime.getTaIdentifier());
        var retrievedT2 = dbHandler.getTransactionNode(t2.getTaIdentifier());
        var retrievedT2Prime = dbHandler.getTransactionNode(t2Prime.getTaIdentifier());
        var retrievedT3 = dbHandler.getTransactionNode(t3.getTaIdentifier());
        Assertions.assertTrue(retrievedT1.getIsValid());
        Assertions.assertTrue(!retrievedT1Prime.getIsValid());
        Assertions.assertTrue(!retrievedT2.getIsValid());
        Assertions.assertTrue(!retrievedT2Prime.getIsValid());
        Assertions.assertTrue(!retrievedT3.getIsValid());

        logger.info("Double-spending was correctly detected.");

        logger.info("Completed cascading invalidations test.");
    }
}
