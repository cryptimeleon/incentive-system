package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockDsTagEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockDsidEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockTransactionEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.mock.MockUserInfoEntryRepository;
import org.cryptimeleon.incentivesystem.dsprotectionservice.storage.TransactionEntry;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

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

        logger.info("Setting up database handler and (implicit) incentive system for the test.");
        IncentivePublicParameters pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        LocalDatabaseHandler dbHandler = new LocalDatabaseHandler(
                pp,
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
                ).size()
                == 0
        );

        logger.info("Generating valid random transaction with associated double-spending tag.");
        var ta1 = Helper.generateRandomTransaction(pp, true);
        logger.info("Adding generated transaction to the database.");
        dbHandler.addTransactionNode(ta1);

        logger.info("Generating invalid random transaction and associated double-spending tag.");
        var ta2 = Helper.generateRandomTransaction(pp, false);
        logger.info("Adding generated transaction to the database.");
        dbHandler.addTransactionNode(ta2);

        logger.info("Checking whether double-spending database contains the correct number of transaction entries.");
        Assertions.assertTrue(
                ((ArrayList<TransactionEntry>)
                        dbHandler.transactionRepository.findAll()
                ).size() == 2
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
        logger.info("Started token adding test.");

        logger.info("Setting up database handler and (implicit) incentive system for the test.");
        IncentivePublicParameters pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        LocalDatabaseHandler dbHandler = new LocalDatabaseHandler(
                pp,
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

        logger.info("Generating random double-spending IDs.");
        GroupElement dsid1 = pp.getBg().getG1().getUniformlyRandomElement();
        GroupElement dsid2 = pp.getBg().getG1().getUniformlyRandomElement();

        logger.info("Adding double-spending IDs to the database.");
        dbHandler.addTokenNode(dsid1);
        dbHandler.addTokenNode(dsid2);

        logger.info("Checking whether double-spending database contains correct number of double-spending IDs.");
        Assertions.assertTrue(
                dbHandler.dsidRepository.count() == 2
        );

        logger.info("Checking whether double-spending database contains both individual double-spending IDs.");
        boolean containsDsid1 = dbHandler.containsTokenNode(dsid1);
        Assertions.assertTrue(containsDsid1);
        boolean containsDsid2 = dbHandler.containsTokenNode(dsid2);
        Assertions.assertTrue(containsDsid2);

        logger.info("Generating random user info for stored double-spending IDs.");
        UserInfo uInfo1 = Helper.generateRandomUserInfo(pp);
        UserInfo uInfo2 = Helper.generateRandomUserInfo(pp);

        logger.info("Linking user info with stored double-spending IDs.");
        dbHandler.addAndLinkUserInfo(uInfo1, dsid1);
        dbHandler.addAndLinkUserInfo(uInfo2, dsid2);

        logger.info("Checking whether double-spending database contains correct number of user info entries.");
        Assertions.assertTrue(
                dbHandler.userInfoRepository.count() == 2
        );

        logger.info("Checking whether double-spending database contains both individual user info entries.");
        UserInfo retrievedUserInfo1 = dbHandler.getUserInfo(dsid1);
        UserInfo retrievedUserInfo2 = dbHandler.getUserInfo(dsid2);
        Assertions.assertTrue(
                uInfo1.equals(retrievedUserInfo1)
        );
        logger.info("User info for first transaction was retrieved correctly");
        Assertions.assertTrue(
                uInfo2.equals(retrievedUserInfo2)
        );
        logger.info("User info for second transaction was retrieved correctly");

        logger.info("Completed token adding test.");
    }

    /**
     * Adds some transactions and tokens to the database, then connects them via edges and
     * checks whether edges are contained and
     * whether consumed tokens/consuming transactions are returned correctly when queried.
     */
    @Test
    public void edgeTest() {
        logger.info("Starting edge test.");

        logger.info("Setting up database handler and (implicit) incentive system for the test.");
        IncentivePublicParameters pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        LocalDatabaseHandler dbHandler = new LocalDatabaseHandler(
                pp,
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
                ).size()
                        == 0
        );

        logger.info("Generating and adding random valid transactions.");
        Transaction ta1 = Helper.generateRandomTransaction(pp, true);
        Transaction ta2 = Helper.generateRandomTransaction(pp, true);
        Transaction ta3 = Helper.generateRandomTransaction(pp, true);
        dbHandler.addTransactionNode(ta1);
        dbHandler.addTransactionNode(ta2);
        dbHandler.addTransactionNode(ta3);

        logger.info("Generating and adding random double-spending IDs.");
        GroupElement dsid1 = pp.getBg().getG1().getUniformlyRandomElement();
        GroupElement dsid2 = pp.getBg().getG1().getUniformlyRandomElement();
        dbHandler.addTokenNode(dsid1);
        dbHandler.addTokenNode(dsid2);

        logger.info("Checking for correct table sizes.");
        Assertions.assertTrue(
                dbHandler.dsidRepository.count() == 2 && dbHandler.transactionRepository.count() == 3
        );

        logger.info("Making edges.");
        dbHandler.addTransactionTokenEdge(ta1.getTaIdentifier(), dsid1);
        dbHandler.addTokenTransactionEdge(dsid1, ta2.getTaIdentifier());
        dbHandler.addTransactionTokenEdge(ta2.getTaIdentifier(), dsid2);
        dbHandler.addTokenTransactionEdge(dsid1, ta3.getTaIdentifier());

        logger.info("Checking database for containing exactly the made edges.");

        boolean containsTa1Dsid1Edge = dbHandler.containsTransactionTokenEdge(ta1.getTaIdentifier(), dsid1);
        boolean containsTa1Dsid2Edge = dbHandler.containsTransactionTokenEdge(ta1.getTaIdentifier(), dsid2);
        boolean containsTa2Dsid1Edge = dbHandler.containsTransactionTokenEdge(ta2.getTaIdentifier(), dsid1);
        boolean containsTa2Dsid2Edge = dbHandler.containsTransactionTokenEdge(ta2.getTaIdentifier(), dsid2);
        boolean containsTa3Dsid1Edge = dbHandler.containsTransactionTokenEdge(ta3.getTaIdentifier(), dsid1);
        boolean containsTa3Dsid2Edge = dbHandler.containsTransactionTokenEdge(ta3.getTaIdentifier(), dsid2);

        boolean containsDsid1Ta1Edge = dbHandler.containsTokenTransactionEdge(dsid1, ta1.getTaIdentifier());
        boolean containsDsid1Ta2Edge = dbHandler.containsTokenTransactionEdge(dsid1, ta2.getTaIdentifier());
        boolean containsDsid1Ta3Edge = dbHandler.containsTokenTransactionEdge(dsid1, ta3.getTaIdentifier());
        boolean containsDsid2Ta1Edge = dbHandler.containsTokenTransactionEdge(dsid2, ta1.getTaIdentifier());
        boolean containsDsid2Ta2Edge = dbHandler.containsTokenTransactionEdge(dsid2, ta2.getTaIdentifier());
        boolean containsDsid2Ta3Edge = dbHandler.containsTokenTransactionEdge(dsid2, ta3.getTaIdentifier());

        Assertions.assertTrue(containsTa1Dsid1Edge);
        Assertions.assertTrue(!containsTa1Dsid2Edge);
        Assertions.assertTrue(!containsTa2Dsid1Edge);
        Assertions.assertTrue(containsTa2Dsid2Edge);
        Assertions.assertTrue(!containsTa3Dsid1Edge);
        Assertions.assertTrue(!containsTa3Dsid2Edge);

        Assertions.assertTrue(!containsDsid1Ta1Edge);
        Assertions.assertTrue(containsDsid1Ta2Edge);
        Assertions.assertTrue(containsDsid1Ta3Edge);
        Assertions.assertTrue(!containsDsid2Ta1Edge);
        Assertions.assertTrue(!containsDsid2Ta2Edge);
        Assertions.assertTrue(!containsDsid2Ta3Edge);

        logger.info("Completed edge test.");
    }

    /**
     * Adds a valid transaction and invalidates it twice.
     * Checks whether transaction is still invalid in the end.
     */
    @Test
    public void invalidateTaTest() {
        logger.info("Starting transaction invalidation test.");

        logger.info("Setting up database handler and (implicit) incentive system for the test.");
        IncentivePublicParameters pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
        LocalDatabaseHandler dbHandler = new LocalDatabaseHandler(
                pp,
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

        logger.info("Generating valid random transaction with associated double-spending tag and add it to the database.");
        var ta1 = Helper.generateRandomTransaction(pp, true);
        logger.info("Adding generated transaction to the database.");
        dbHandler.addTransactionNode(ta1);

        logger.info("Check whether transaction is valid.");
        Transaction retrievedTa = dbHandler.getTransactionNode(ta1.getTaIdentifier());
        Assertions.assertTrue(retrievedTa.getIsValid());

        logger.info("Invalidating transaction twice and checking that it is invalid afterwards.");
        for(int i = 0; i < 2; i++) {
            dbHandler.invalidateTransaction(ta1.getTaIdentifier());
            retrievedTa = dbHandler.getTransactionNode(ta1.getTaIdentifier());
            Assertions.assertTrue(!retrievedTa.getIsValid());
        }

        logger.info("Completed transaction invalidation test.");
    }
}
