package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.junit.jupiter.api.BeforeAll;

/**
 * Extends the existing spend test by a test of the double-spending protection database.
 * Transaction data is always recorded in the database and the double-spending detection mechanism is triggered.
 */
@Slf4j
public class DsProtectionIntegrationTest extends SpendTest{
    DSProtectionClient dsProtectionClient;

    /**
     * Executed before the first test of a session.
     * Sets up all clients and crypto assets (user/provider key pairs, ...)
     * as well as the incentive system and some test basket items and promotions.
     */
    @BeforeAll
    void prepareBasketAndPromotions() {
        super.prepareBasketAndPromotions();
        this.dsProtectionClient = new DSProtectionClient(dsProtectionUrl);
    }
}
