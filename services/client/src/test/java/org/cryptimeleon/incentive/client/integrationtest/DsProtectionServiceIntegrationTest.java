package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigInteger;
import org.cryptimeleon.math.structures.cartesian.Vector;

/**
 * Tests the double-spending protection service by performing both honest and malicious transactions
 * and checking whether they were correctly recorded in the database.
 *
 * Extends the SpendTest so that we do not have to rewrite all the setup stuff (which would be identical).
 * Setup is done using a BeforeEach-annotated method in the SpendTest class.
 */
public class DsProtectionServiceIntegrationTest extends SpendTest {
    private DSProtectionClient dsProtectionClient;

    @BeforeAll
    void testSetup() {
        // setup clients, crypto assets, basket and promotions (identical to spend test)
        prepareBasketAndPromotions();

        // create dsprotection client (only needed in dsprotection test)
        dsProtectionClient = new DSProtectionClient(dsProtectionUrl);
    }

    /**
     * Clears all transactions from possible previous test runs from the database.
     */
    @BeforeEach
    public void clearDatabase() {
        // REST call
        dsProtectionClient.clearDatabase();
    }

    /**
     * Does an honest Spend-Deduct interaction with the incentive service
     * and checks afterwards whether it was correctly recorded as a transaction in the database.
     */
    public void honestTransactionTest() {
        // generate token and basket
        var token = generateToken(testPromotion.getPromotionParameters(), Vector.of(BigInteger.valueOf(42)));
        var basketId = createBasket();
        assert basketId != null;

        // run Spend-Deduct protocol
        runSpendDeductWorkflow(token, basketId);

        // query recorded transaction object from database
    }

    /**
     * Creates a token and spends it twice,
     * then checks whether second transaction was recorded as invalid in database.
     */
    public void doubleSpendingTest() {
        
    }
}
