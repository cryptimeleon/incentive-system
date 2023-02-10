package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.callback.ITransactionDBHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.UUID;

public class RejectionAndDSProtectionTest {
    IncentiveSystem incSys = TestSuite.incentiveSystem;
    UUID basketId = UUID.randomUUID();
    UUID secondBasketId = UUID.randomUUID();
    PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(2);
    Vector<BigInteger> pointsBeforeSpend = Vector.of(BigInteger.valueOf(10L), BigInteger.valueOf(0L));
    Vector<BigInteger> pointsAfterSpend = Vector.of(BigInteger.valueOf(6L), BigInteger.valueOf(0L));
    Vector<BigInteger> pointDifference = pointsBeforeSpend.zip(pointsAfterSpend, BigInteger::subtract);
    Vector<BigInteger> pointsAfterSpendAlt = Vector.of(BigInteger.valueOf(4L), BigInteger.valueOf(0L));
    Vector<BigInteger> pointDifferenceAlt = pointsBeforeSpend.zip(pointsAfterSpendAlt, BigInteger::subtract);

    Token token = TestSuite.generateToken(promotionParameters, pointsBeforeSpend);
    TestSuite.TestTransactionDbHandler transactionDBHandler;
    TestSuite.TestDsidBlacklist testDsidBlacklist;
    TestRedeemedHandler testRedeemedHandler;

    @BeforeEach
    void setup() {
        transactionDBHandler = new TestSuite.TestTransactionDbHandler();
        testDsidBlacklist = new TestSuite.TestDsidBlacklist();
        testRedeemedHandler = new TestRedeemedHandler();
    }

    @Test
    void testSuccessfulRetryAtProvider() {
        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, transactionDBHandler);
        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, transactionDBHandler);
    }

    @Test
    void testSuccessfulRejectionAtStoreSameBasketDifferentRequest() {
        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, transactionDBHandler);

        // Same basket, different request
        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendToken(basketId, testDsidBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler, transactionDBHandler));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("Basket already redeemed for different request"));
    }

    @Test
    void testSuccessfulRejectionAtStoreDifferentBasketSameRequest() {
        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, transactionDBHandler);

        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendToken(secondBasketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, transactionDBHandler));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("already spent with different basket"));
    }

    @Test
    void testSuccessfulRejectionAtStoreDifferentBasketAndRequest() {
        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, transactionDBHandler);

        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendToken(secondBasketId, testDsidBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler, transactionDBHandler));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("already spent with different basket"));
    }

    @Test
    void testSuccessfulRejectionAtProvider() {
        // Do no sync blacklist with store => multiple blacklists
        TestSuite.TestDsidBlacklist storeOneBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist storeTwoBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist providerBlacklist = new TestSuite.TestDsidBlacklist();

        spendTokenMultipleBlackslists(basketId, storeOneBlacklist, providerBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, transactionDBHandler);

        Throwable t = Assertions.assertThrows(
                RuntimeException.class,
                () -> spendTokenMultipleBlackslists(secondBasketId, storeTwoBlacklist, providerBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler, transactionDBHandler)
        );
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("Illegal retry, dsid already used for different request"));
    }

    @Test
    void testSuccessfulLink() {
        TestSuite.TestDsidBlacklist storeOneBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist storeTwoBlacklist = new TestSuite.TestDsidBlacklist();

        spendTokenStoreOnly(basketId, storeOneBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, transactionDBHandler);
        spendTokenStoreOnly(secondBasketId, storeTwoBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler, transactionDBHandler);
        SpendTransactionData first = transactionDBHandler.spendData.get(0);
        SpendTransactionData second = transactionDBHandler.spendData.get(1);

        DoubleSpendingTag doubleSpendingTag = new DoubleSpendingTag(first.getC(), first.getGamma());
        DoubleSpendingTag secondDoubleSpendingTag = new DoubleSpendingTag(second.getC(), second.getGamma());
        var linkOutput = incSys.link(TestSuite.pp, doubleSpendingTag, secondDoubleSpendingTag);

        Assertions.assertEquals(linkOutput.getDsBlame(), TestSuite.userKeyPair.getSk().getUsk());
        Assertions.assertEquals(linkOutput.getUpk(), TestSuite.userKeyPair.getPk());
    }

    private SpendCouponSignature spendTokenStoreOnly(UUID basketId,
                                                     IDsidBlacklistHandler dsidBlacklistHandler,
                                                     Vector<BigInteger> pointDifference,
                                                     Vector<BigInteger> pointsAfterSpend,
                                                     IStoreBasketRedeemedHandler storeBasketRedeemedHandler,
                                                     ITransactionDBHandler transactionDBHandler) {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendCouponRequest spendCouponRequest = incSys.generateStoreSpendRequest(
                TestSuite.userKeyPair, TestSuite.providerKeyPair.getPk(), token,
                promotionParameters, basketId, pointsAfterSpend,
                spendDeductTree,
                TestSuite.context
        );
        return incSys.signSpendCoupon(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendCouponRequest,
                spendDeductTree,
                TestSuite.context, storeBasketRedeemedHandler,
                dsidBlacklistHandler,
                transactionDBHandler
        );
    }

    private SpendResponseECDSA spendToken(UUID basketId,
                                          IDsidBlacklistHandler dsidBlacklistHandler,
                                          Vector<BigInteger> pointDifference,
                                          Vector<BigInteger> pointsAfterSpend,
                                          IStoreBasketRedeemedHandler storeBasketRedeemedHandler,
                                          ITransactionDBHandler transactionDBHandler) {
        return spendTokenMultipleBlackslists(basketId,
                dsidBlacklistHandler,
                dsidBlacklistHandler,
                pointDifference,
                pointsAfterSpend,
                storeBasketRedeemedHandler,
                transactionDBHandler);
    }

    /*
     * For a tests where dsid blacklists are not synchronized immediately
     */
    private SpendResponseECDSA spendTokenMultipleBlackslists(UUID basketId,
                                                             IDsidBlacklistHandler storeBlacklist,
                                                             IDsidBlacklistHandler providerBlacklist,
                                                             Vector<BigInteger> pointDifference,
                                                             Vector<BigInteger> pointsAfterSpend,
                                                             IStoreBasketRedeemedHandler testRedeemedHandler,
                                                             ITransactionDBHandler transactionDBHandler) {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendCouponRequest spendCouponRequest = incSys.generateStoreSpendRequest(
                TestSuite.userKeyPair,
                TestSuite.providerKeyPair.getPk(),
                token,
                promotionParameters,
                basketId,
                pointsAfterSpend,
                spendDeductTree,
                TestSuite.context
        );
        SpendCouponSignature spendCouponSignature = incSys.signSpendCoupon(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendCouponRequest,
                spendDeductTree,
                TestSuite.context, testRedeemedHandler,
                storeBlacklist,
                transactionDBHandler
        );
        Assertions.assertTrue(incSys.verifySpendCouponSignature(spendCouponRequest, spendCouponSignature, promotionParameters, basketId));

        SpendRequestECDSA spendRequest = new SpendRequestECDSA(spendCouponRequest, spendCouponSignature);
        return incSys.verifySpendRequestAndIssueNewToken(
                TestSuite.providerKeyPair,
                promotionParameters,
                spendRequest,
                basketId,
                spendDeductTree,
                TestSuite.context,
                (z) -> true,
                providerBlacklist
        );
    }
}
