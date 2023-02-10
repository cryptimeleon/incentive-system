package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void testSuccessfulRetryAtProvider() {
        TestSuite.TestDsidBlacklist testDsidBlacklist = new TestSuite.TestDsidBlacklist();
        TestRedeemedHandler testRedeemedHandler = new TestRedeemedHandler();

        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);
        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);
    }

    @Test
    void testSuccessfulRejectionAtStoreSameBasketDifferentRequest() {
        TestSuite.TestDsidBlacklist testDsidBlacklist = new TestSuite.TestDsidBlacklist();
        TestRedeemedHandler testRedeemedHandler = new TestRedeemedHandler();

        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);

        // Same basket, different request
        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendToken(basketId, testDsidBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("Basket already redeemed for different request"));
    }

    @Test
    void testSuccessfulRejectionAtStoreDifferentBasketSameRequest() {
        TestSuite.TestDsidBlacklist testDsidBlacklist = new TestSuite.TestDsidBlacklist();
        TestRedeemedHandler testRedeemedHandler = new TestRedeemedHandler();

        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);

        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendToken(secondBasketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("already spent with different basket"));
    }

    @Test
    void testSuccessfulRejectionAtStoreDifferentBasketAndRequest() {
        TestSuite.TestDsidBlacklist testDsidBlacklist = new TestSuite.TestDsidBlacklist();
        TestRedeemedHandler testRedeemedHandler = new TestRedeemedHandler();

        spendToken(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);

        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendToken(secondBasketId, testDsidBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("already spent with different basket"));
    }

    @Test
    void testSuccessfulRejectionAtProvider() {
        TestSuite.TestDsidBlacklist storeOneBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist storeTwoBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist providerBlacklist = new TestSuite.TestDsidBlacklist();
        TestRedeemedHandler testRedeemedHandler = new TestRedeemedHandler();

        spendTokenMultipleBlackslists(basketId, storeOneBlacklist, providerBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);
        // TODO do no sync blacklist with store

        Throwable t = Assertions.assertThrows(
                RuntimeException.class,
                () -> spendTokenMultipleBlackslists(secondBasketId, storeTwoBlacklist, providerBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler)
        );
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("Illegal retry, dsid already used for different request"));
    }

    @Test
    void testSuccessfulLink() {
        TestRedeemedHandler testRedeemedHandler = new TestRedeemedHandler();
        TestSuite.TestDsidBlacklist storeOneBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist storeTwoBlacklist = new TestSuite.TestDsidBlacklist();

        SpendClearingData first = spendTokenStoreOnly(basketId, storeOneBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler).spendClearingData;
        SpendClearingData second = spendTokenStoreOnly(secondBasketId, storeTwoBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler).spendClearingData;
        DoubleSpendingTag doubleSpendingTag = new DoubleSpendingTag(first.getC(), first.getGamma());
        DoubleSpendingTag secondDoubleSpendingTag = new DoubleSpendingTag(second.getC(), second.getGamma());
        var linkOutput = incSys.link(TestSuite.pp, doubleSpendingTag, secondDoubleSpendingTag);

        Assertions.assertEquals(linkOutput.getDsBlame(), TestSuite.userKeyPair.getSk().getUsk());
        Assertions.assertEquals(linkOutput.getUpk(), TestSuite.userKeyPair.getPk());
    }

    private SpendStoreOutput spendTokenStoreOnly(UUID basketId, IDsidBlacklistHandler dsidBlacklistHandler, Vector<BigInteger> pointDifference, Vector<BigInteger> pointsAfterSpend, IStoreBasketRedeemedHandler storeBasketRedeemedHandler) {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendCouponRequest spendCouponRequest = incSys.generateStoreSpendRequest(
                token,
                TestSuite.userKeyPair,
                pointsAfterSpend,
                TestSuite.providerKeyPair.getPk(),
                promotionParameters,
                basketId,
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
                storeBasketRedeemedHandler,
                dsidBlacklistHandler,
                TestSuite.context
        );
    }

    private SpendResponseECDSA spendToken(UUID basketId, IDsidBlacklistHandler dsidBlacklistHandler, Vector<BigInteger> pointDifference, Vector<BigInteger> pointsAfterSpend, IStoreBasketRedeemedHandler storeBasketRedeemedHandler) {
        return spendTokenMultipleBlackslists(basketId, dsidBlacklistHandler, dsidBlacklistHandler, pointDifference, pointsAfterSpend, storeBasketRedeemedHandler);
    }

    /*
     * For a tests where dsid blacklists are not synchronized immediately
     */
    private SpendResponseECDSA spendTokenMultipleBlackslists(UUID basketId, IDsidBlacklistHandler storeBlacklist, IDsidBlacklistHandler providerBlacklist, Vector<BigInteger> pointDifference, Vector<BigInteger> pointsAfterSpend, IStoreBasketRedeemedHandler testRedeemedHandler) {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendCouponRequest spendCouponRequest = incSys.generateStoreSpendRequest(
                token,
                TestSuite.userKeyPair,
                pointsAfterSpend,
                TestSuite.providerKeyPair.getPk(),
                promotionParameters,
                basketId,
                spendDeductTree,
                TestSuite.context
        );
        SpendStoreOutput spendStoreOutput = incSys.signSpendCoupon(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendCouponRequest,
                spendDeductTree,
                testRedeemedHandler,
                storeBlacklist,
                TestSuite.context
        );
        SpendCouponSignature spendCouponSignature = spendStoreOutput.spendCouponSignature;
        Assertions.assertTrue(incSys.verifySpendCouponSignature(spendCouponRequest, spendCouponSignature, promotionParameters, basketId));

        SpendRequestECDSA spendRequest = new SpendRequestECDSA(spendCouponRequest, spendCouponSignature, promotionParameters, basketId);
        return incSys.verifySpendRequestAndIssueNewToken(
                TestSuite.providerKeyPair,
                spendRequest,
                promotionParameters,
                (z) -> true,
                providerBlacklist,
                spendDeductTree,
                TestSuite.context
        );
    }
}
