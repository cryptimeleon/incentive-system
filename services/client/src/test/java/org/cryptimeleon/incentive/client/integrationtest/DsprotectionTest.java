package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.springframework.beans.factory.annotation.Value;

/**
 * Tests the methods for interacting with the double-spending database.
 */
public class DsprotectionTest {
    @Value("${dsprotection.url}")
    public String dsprotectionUrl;

    /**
     * Adds some transactions to the database.
     */
    public void addTransactionTest() {
        // create client
        var dsprotectionClient = new DSProtectionClient(dsprotectionUrl);

        // generate public parameters for (implicit) incentive system instance
        IncentivePublicParameters pp = Setup.trustedSetup(512, Setup.BilinearGroupChoice.Debug);

        // create transaction
        var ta1 = Helper.generateTransaction(pp, true);

        // TODO: add transaction to DB
    }
}
