package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.*;

import java.math.BigInteger;

/**
 * Tests the double-spending protection service by performing both honest and malicious transactions
 * and checking whether they were correctly recorded in the database.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DsProtectionServiceIntegrationTest extends TransactionTestPreparation {
    private DSProtectionClient dsProtectionClient;

    @BeforeAll
    void testSetup() {
        // setup test basket items and promotions
        super.prepareBasketServiceAndPromotions();

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
    @Test
    public void honestTransactionTest() throws InterruptedException {
        log.info("Started honest transaction integration test.");

        log.info("Generate token and basket.");
        var token = generateToken(testPromotion.getPromotionParameters(), Vector.of(BigInteger.valueOf(20)));
        var basketId = createBasket();
        assert basketId != null;

        log.info("Spend-Deduct protocol running...");
        TransactionIdentifier occuredTaId = runSpendDeductWorkflow(token, basketId, true);

        Thread.sleep(2500);

        log.info("Done.");
        log.info("Query recorded transaction from database.");
        String serializedOccuredTaRepr = dsProtectionClient.getTransaction(occuredTaId);

        log.info("Deserialize and restore transaction.");
        JSONConverter jsonConverter = new JSONConverter();
        Representation occuredTaRepr = jsonConverter.deserialize(serializedOccuredTaRepr);
        Transaction occuredTa = new Transaction(occuredTaRepr, cryptoAssets.getPublicParameters());

        log.info("Verify that transaction is valid.");
        Assertions.assertTrue(occuredTa.getIsValid());

        log.info("Finished honest transaction integration test.");
    }

    /**
     * Creates a token and spends it twice,
     * then checks whether second transaction was recorded as invalid in database.
     */
    @Test
    public void doubleSpendingTest() throws InterruptedException {
        log.info("Started double-spending integration test.");

        log.info("Generate token to be double-spended and baskets.");
        var token = generateToken(testPromotion.getPromotionParameters(), Vector.of(BigInteger.valueOf(20)));
        var basketId1 = createBasket();
        assert basketId1 != null;
        var basketId2 = createBasket();
        assert basketId2 != null;

        log.info("Spend-Deduct protocol running...");
        TransactionIdentifier occuredTaId1 = runSpendDeductWorkflow(token, basketId1, true);
        log.info("Done.");
        log.info("Double-spending Spend-Deduct execution running...");
        TransactionIdentifier occuredTaId2 = runSpendDeductWorkflow(token, basketId2, true);
        log.info("Done.");

        Thread.sleep(2500);

        log.info("Query recorded transactions from database.");
        String serializedOccuredTaRepr1 = dsProtectionClient.getTransaction(occuredTaId1);
        String serializedOccuredTaRepr2 = dsProtectionClient.getTransaction(occuredTaId2);

        log.info("Deserialize and restore transaction.");
        JSONConverter jsonConverter = new JSONConverter();
        Representation occuredTaRepr1 = jsonConverter.deserialize(serializedOccuredTaRepr1);
        Representation occuredTaRepr2 = jsonConverter.deserialize(serializedOccuredTaRepr2);
        Transaction occuredTa1 = new Transaction(occuredTaRepr1, cryptoAssets.getPublicParameters());
        Transaction occuredTa2 = new Transaction(occuredTaRepr2, cryptoAssets.getPublicParameters());

        log.info("Verify that double-spending was correctly detected.");
        Assertions.assertTrue(occuredTa1.getIsValid());
        Assertions.assertFalse(occuredTa2.getIsValid());

        log.info("Finished double-spending integration test.");
    }
}
