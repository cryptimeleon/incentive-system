package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.*;

import java.math.BigInteger;
import org.cryptimeleon.math.structures.cartesian.Vector;

/**
 * Tests the double-spending protection service by performing both honest and malicious transactions
 * and checking whether they were correctly recorded in the database.
 *
 * Extends the SpendTest so that we do not have to rewrite all the setup stuff (which would be identical).
 * Setup is done using a BeforeEach-annotated method in the SpendTest class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DsProtectionServiceIntegrationTest extends TransactionTestPreparation {
    private DSProtectionClient dsProtectionClient;

    @BeforeAll
    void testSetup() {
        // setup test basket items and promotions
        super.prepareBasketAndPromotions();

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
    public void honestTransactionTest() {
        // generate token and basket
        var token = generateToken(testPromotion.getPromotionParameters(), Vector.of(BigInteger.valueOf(20)));
        var basketId = createBasket();
        assert basketId != null;

        // run Spend-Deduct protocol
        TransactionIdentifier occuredTaId = runSpendDeductWorkflow(token, basketId);

        // query recorded transaction object from database
        String serializedOccuredTaRepr = dsProtectionClient.getTransaction(occuredTaId);

        // deserialize and restore it
        JSONConverter jsonConverter = new JSONConverter();
        Representation occuredTaRepr = jsonConverter.deserialize(serializedOccuredTaRepr);
        Transaction occuredTa = new Transaction(occuredTaRepr, cryptoAssets.getPublicParameters());

        // assert that transaction is valid
        Assertions.assertTrue(occuredTa.getIsValid());
    }

    /**
     * Creates a token and spends it twice,
     * then checks whether second transaction was recorded as invalid in database.
     */
    @Test
    public void doubleSpendingTest() {
        // generate token and baskets
        var token = generateToken(testPromotion.getPromotionParameters(), Vector.of(BigInteger.valueOf(42)));
        var basketId1 = createBasket();
        assert basketId1 != null;
        var basketId2 = createBasket();
        assert basketId2 != null;

        // run Spend-Deduct protocol twice with same token
        TransactionIdentifier occuredTaId1 = runSpendDeductWorkflow(token, basketId1);
        TransactionIdentifier occuredTaId2 = runSpendDeductWorkflow(token, basketId2);

        // query recorded transaction objects from database
        String serializedOccuredTaRepr1 = dsProtectionClient.getTransaction(occuredTaId1);
        String serializedOccuredTaRepr2 = dsProtectionClient.getTransaction(occuredTaId2);

        // deserialize and restore them
        JSONConverter jsonConverter = new JSONConverter();
        Representation occuredTaRepr1 = jsonConverter.deserialize(serializedOccuredTaRepr1);
        Representation occuredTaRepr2 = jsonConverter.deserialize(serializedOccuredTaRepr2);
        Transaction occuredTa1 = new Transaction(occuredTaRepr1, cryptoAssets.getPublicParameters());
        Transaction occuredTa2 = new Transaction(occuredTaRepr2, cryptoAssets.getPublicParameters());

        // assert that transaction is valid
        Assertions.assertTrue(occuredTa1.getIsValid());
        Assertions.assertFalse(occuredTa2.getIsValid());
    }
}
