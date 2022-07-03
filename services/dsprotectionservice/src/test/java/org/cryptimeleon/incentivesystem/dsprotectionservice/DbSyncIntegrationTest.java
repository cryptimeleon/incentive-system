package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockDsTagEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockDsidEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockTransactionEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockUserInfoEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionEntry;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Tests for the DB Sync algorithm using dummy transactions.
 */
public class DbSyncIntegrationTest {
    private final Logger logger = LoggerFactory.getLogger(DbSyncIntegrationTest.class);

    /**
     * Syncing two transactions into the DB that spend different tokens.
     */
    @Test
    void insertHonestTransactionTest() {
        logger.info("Starting honest transactions test.");

        logger.info("Setup incentive system and database handler for the test.");
        var pp = IncentiveSystem.setup(256, Setup.BilinearGroupChoice.Debug);
        var incSys = new IncentiveSystem(pp);
        var promotionId = BigInteger.ONE;
        var dbHandler = new LocalDatabaseHandler(
                incSys.getPp(),
                new MockDsidEntryRepository(),
                new MockTransactionEntryRepository(),
                new MockDsTagEntryRepository(),
                new MockUserInfoEntryRepository()
        );

        logger.info("Clear database."); // needed if this is not the first test in a sequence of tests
        dbHandler.clearDatabase();
        Assertions.assertEquals(
                0, ((ArrayList<TransactionEntry>) dbHandler.transactionRepository.findAll()).size()
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
                ta1.getUserChoice(),
                promotionId,
                dbHandler
        );
        incSys.dbSync(
                ta2.getTransactionID(),
                dsid2,
                ta2.getDsTag(),
                ta2.getUserChoice(),
                promotionId,
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
        Assertions.assertFalse(dbHandler.containsTokenTransactionEdge(dsid1, ta2.getTaIdentifier()));
        Assertions.assertFalse(dbHandler.containsTokenTransactionEdge(dsid2, ta1.getTaIdentifier()));
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
     *
     * The PRFtoZn images hashedClaim that are used
     */
    @Test
    void cascadingInvalidationsTest() {
        logger.info("Starting cascading invalidations test.");

        logger.info("Setup incentive system, provider and user key pair and database handler for the test.");
        var pp = IncentiveSystem.setup(256, Setup.BilinearGroupChoice.Debug);
        var incSys = new IncentiveSystem(pp);
        var promotionId = BigInteger.ONE;

        var pkp = Setup.providerKeyGen(incSys.getPp());

        var ukp = Setup.userKeyGen(incSys.getPp());

        var dbHandler = new LocalDatabaseHandler(
                incSys.getPp(),
                new MockDsidEntryRepository(),
                new MockTransactionEntryRepository(),
                new MockDsTagEntryRepository(),
                new MockUserInfoEntryRepository()
        );

        logger.info("Clear database."); // needed if this is not the first test in a sequence of tests
        dbHandler.clearDatabase();
        Assertions.assertEquals(
                0, ((ArrayList<TransactionEntry>) dbHandler.transactionRepository.findAll()).size()
        );

        logger.info("Generating transactions and dsids by executing spend-deduct several times.");
        PromotionParameters legacyPromotionParameters = incSys.legacyPromotionParameters(); // the promotion parameters to always use throughout this test
        SpendDeductTree legacyZkpTree = Helper.generateSimpleTestSpendDeductZkp( // the spend deduct zkp to include in the spend requests, legacy = just collecting and spending a single type of points
                legacyPromotionParameters,
                new Vector<>(new BigInteger("1")) // subtract 1 point from token per spend, generate a token with 10 points
        );
        Token initialToken = Helper.generateToken( // the initial token that the user starts off with
                incSys.getPp(),
                ukp,
                pkp,
                legacyPromotionParameters,
                new Vector<>(new BigInteger("10")) // ten points are more than enough for this test, ensures that user always has enough points
        );

        var spendDeductOutputT1 = Helper.generateSoundTransaction(
                incSys,
                legacyPromotionParameters,
                initialToken,
                pkp,
                ukp,
                new Vector<>(new BigInteger("9")), // initial token holds 10 points, we spend one in every transaction
                incSys.getPp().getBg().getZn().getUniformlyRandomElement(), // random transaction ID
                legacyZkpTree
        );

        var spendDeductOutputT1Prime = Helper.generateSoundTransaction(
                incSys,
                legacyPromotionParameters,
                initialToken,
                pkp,
                ukp,
                new Vector<>(new BigInteger("9")), // initial token holds 10 points, we spend one in every transaction
                incSys.getPp().getBg().getZn().getUniformlyRandomElement(), // random transaction ID
                legacyZkpTree
        );

        var spendDeductOutputT2 = Helper.generateSoundTransaction(
                incSys,
                legacyPromotionParameters,
                spendDeductOutputT1Prime.getResultToken(),
                pkp,
                ukp,
                new Vector<>(new BigInteger("8")), // initial token holds 10 points, we spend one in every transaction
                incSys.getPp().getBg().getZn().getUniformlyRandomElement(), // random transaction ID
                legacyZkpTree
        );

        var spendDeductOutputT2Prime = Helper.generateSoundTransaction(
                incSys,
                legacyPromotionParameters,
                spendDeductOutputT1Prime.getResultToken(),
                pkp,
                ukp,
                new Vector<>(new BigInteger("8")), // initial token holds 10 points, we spend one in every transaction
                incSys.getPp().getBg().getZn().getUniformlyRandomElement(), // random transaction ID
                legacyZkpTree
        );

        var spendDeductOutputT3 = Helper.generateSoundTransaction(
                incSys,
                legacyPromotionParameters,
                spendDeductOutputT2.getResultToken(),
                pkp,
                ukp,
                new Vector<>(new BigInteger("7")), // initial token holds 10 points, we spend one in every transaction
                incSys.getPp().getBg().getZn().getUniformlyRandomElement(), // random transaction ID
                legacyZkpTree
        );

        var t1 = spendDeductOutputT1.getOccuredTransaction();
        var t1Prime = spendDeductOutputT1Prime.getOccuredTransaction();
        var t2 = spendDeductOutputT2.getOccuredTransaction();
        var t2Prime = spendDeductOutputT2Prime.getOccuredTransaction();
        var t3 = spendDeductOutputT3.getOccuredTransaction();
        var dsid1 = initialToken.computeDsid(incSys.getPp());
        var dsid2 = spendDeductOutputT1Prime.getResultToken().computeDsid(incSys.getPp()); // we want dsid2 to be the successor of t1Prime, as in the graph from the paper
        var dsid3 = spendDeductOutputT2.getResultToken().computeDsid(incSys.getPp()); // we want dsid3 to be the successor of t2, as in the graph from the paper

        logger.info("Syncing transactions and dsids to database.");
        logger.info("Syncing some honest spend transactions.");
        logger.info("Syncing t2Prime which spent dsid2");
        incSys.dbSync( // t2Prime and dsid2
                t2Prime.getTaIdentifier().getTid(),
                dsid2,
                t2Prime.getDsTag(),
                t2Prime.getUserChoice(),
                promotionId,
                dbHandler
        );
        logger.info("Syncing t1 which spent dsid1");
        incSys.dbSync( // t1 and dsid1
                t1.getTaIdentifier().getTid(),
                dsid1,
                t1.getDsTag(),
                t1.getUserChoice(),
                promotionId,
                dbHandler
        );
        logger.info("Syncing t3 which spent dsid3");
        incSys.dbSync( // t3 and dsid3
                t3.getTaIdentifier().getTid(),
                dsid3,
                t3.getDsTag(),
                t3.getUserChoice(),
                promotionId,
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
                t1.getUserChoice(),
                promotionId,
                dbHandler
        );
        logger.info("Syncing t2 which spent dsid2");
        incSys.dbSync( // t2 and dsid2
                t2.getTaIdentifier().getTid(),
                dsid2,
                t2.getDsTag(),
                t2Prime.getUserChoice(),
                promotionId,
                dbHandler
        );
        logger.info("Done syncing transactions and dsids to database.");

        logger.info("Checking whether double spending was correctly detected.");

        logger.info("Checking validity of transactions.");
        var retrievedT1 = dbHandler.getTransactionNode(t1.getTaIdentifier());
        var retrievedT1Prime = dbHandler.getTransactionNode(t1Prime.getTaIdentifier());
        var retrievedT2 = dbHandler.getTransactionNode(t2.getTaIdentifier());
        var retrievedT2Prime = dbHandler.getTransactionNode(t2Prime.getTaIdentifier());
        var retrievedT3 = dbHandler.getTransactionNode(t3.getTaIdentifier());
        Assertions.assertTrue(retrievedT1.getIsValid());
        Assertions.assertFalse(retrievedT1Prime.getIsValid());
        Assertions.assertFalse(retrievedT2.getIsValid());
        Assertions.assertFalse(retrievedT2Prime.getIsValid());
        Assertions.assertFalse(retrievedT3.getIsValid());

        logger.info("Checking final token count.");
        Assertions.assertEquals(5, dbHandler.getTokenCount());

        logger.info("Double-spending was correctly detected.");

        logger.info("Completed cascading invalidations test.");
    }
}
