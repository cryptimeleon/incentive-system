package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Tests the methods for interacting with the double-spending database.
 */
public class DsprotectionTest {
    @Value("${dsprotection.url}")
    public String dsprotectionUrl;

    private Logger logger = LoggerFactory.getLogger(DSProtectionClient.class);

    /**
     * Adds some transactions to the database.
     */
    @Test
    public void addTransactionTest() {
        logger.info("ADD TRANSACTION TEST STARTED");

        logger.info("Generating client.");

        // create client
        var dsprotectionClient = new DSProtectionClient(this.dsprotectionUrl);

        logger.info("Generating public parameters.");

        // generate public parameters for (implicit) incentive system instance
        IncentivePublicParameters pp = Setup.trustedSetup(512, Setup.BilinearGroupChoice.Debug);

        logger.info("Adding first randomly-generated transaction.");

        // create valid transaction
        var ta1 = Helper.generateTransaction(pp, true);

        // add transaction to DB
        dsprotectionClient.addTransactionNode(ta1);

        // check whether transaction was correctly added
        Assertions.assertTrue(
                dsprotectionClient.containsTransactionNode(ta1.getTransactionID(), ta1.getDsTag().getGamma())
        );

        logger.info("Adding second randomly-generated transaction.");

        // add invalid transaction to DB and check whether it was correctly added
        var ta2 = Helper.generateTransaction(pp, false);
        dsprotectionClient.addTransactionNode(ta2);
        Assertions.assertTrue(dsprotectionClient.containsTransactionNode(ta1.getTransactionID(), ta2.getDsTag().getGamma()));
    }

    @Test
    public void addTokenTest() {
        System.out.println("ADD TOKEN TEST");

        // create client
        // TODO: URL hard-coded atm, since reading from application.properties does not seem to work.
        var dsprotectionClient = new DSProtectionClient("http://localhost:8004");

        // generate public parameters for implicit incentive system instance
        IncentivePublicParameters pp = Setup.trustedSetup(512, Setup.BilinearGroupChoice.Debug);

        // generate dummy dsid
        var dummyDsid = pp.getBg().getG1().getUniformlyRandomElement();

        // add dsid to DB
        dsprotectionClient.addTokenNode(dummyDsid);

        // check whether token node was correctly added
        Assertions.assertTrue(
                dsprotectionClient.containsTokenNode(dummyDsid)
        );

        // add another dummy dsid to DB
        var dummyDsid2 = pp.getBg().getG1().getUniformlyRandomElement();
        dsprotectionClient.addTokenNode(dummyDsid2);
        Assertions.assertTrue(
                dsprotectionClient.containsTokenNode(dummyDsid2)
        );
    }
}
