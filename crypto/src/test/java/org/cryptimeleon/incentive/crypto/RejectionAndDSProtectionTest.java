package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.incentive.crypto.callback.ISpendTransactionDBHandler;
import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

public class RejectionAndDSProtectionTest {
    final IncentiveSystem incSys = TestSuite.incentiveSystem;
    final UUID basketId = UUID.randomUUID();
    final UUID secondBasketId = UUID.randomUUID();
    final PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(2);
    final Vector<BigInteger> pointsBeforeSpend = Vector.of(BigInteger.valueOf(10L), BigInteger.valueOf(0L));
    final Vector<BigInteger> pointsAfterSpend = Vector.of(BigInteger.valueOf(6L), BigInteger.valueOf(0L));
    final Vector<BigInteger> pointDifference = pointsBeforeSpend.zip(pointsAfterSpend, BigInteger::subtract);
    final Vector<BigInteger> pointsAfterSpendAlt = Vector.of(BigInteger.valueOf(4L), BigInteger.valueOf(0L));
    final Vector<BigInteger> pointDifferenceAlt = pointsBeforeSpend.zip(pointsAfterSpendAlt, BigInteger::subtract);

    final Token token = TestSuite.generateToken(promotionParameters, pointsBeforeSpend);
    TestSuite.TestDsidBlacklist testDsidBlacklist;
    TestRedeemedHandler testRedeemedHandler;
    TestRedeemedHandler secondTestRedeemedHandler;

    @BeforeEach
    void setup() {
        testDsidBlacklist = new TestSuite.TestDsidBlacklist();
        testRedeemedHandler = new TestRedeemedHandler();
        secondTestRedeemedHandler = new TestRedeemedHandler();
    }

    @Test
    void testSuccessfulRetryAtProvider() {
        spendTokenSyncronizedBlacklists(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);
        spendTokenSyncronizedBlacklists(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);
    }

    @Test
    void testSuccessfulRejectionAtStoreSameBasketDifferentRequest() {
        spendTokenStoreOnly(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, spendTransactionData -> {
        });

        // Same basket, different request
        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendTokenStoreOnly(basketId, testDsidBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler, spendTransactionData -> {
        }));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("Basket already redeemed for different request"));
    }

    @Test
    void testSuccessfulRejectionAtStoreDifferentBasketSameRequest() {
        spendTokenStoreOnly(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, spendTransactionData -> {
        });

        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendTokenStoreOnly(secondBasketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, spendTransactionData -> {
        }));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("already spent with different basket"));
    }

    @Test
    void testSuccessfulRejectionAtStoreDifferentBasketAndRequest() {
        spendTokenStoreOnly(basketId, testDsidBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, spendTransactionData -> {
        });

        Throwable t = Assertions.assertThrows(RuntimeException.class, () -> spendTokenStoreOnly(secondBasketId, testDsidBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler, spendTransactionData -> {
        }));
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("already spent with different basket"));
    }

    @Test
    void testSuccessfulRejectionAtProvider() {
        // Do no sync blacklist with store => multiple blacklists
        TestSuite.TestDsidBlacklist storeOneBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist storeTwoBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist providerBlacklist = new TestSuite.TestDsidBlacklist();

        spendTokenMultipleBlackslists(basketId, storeOneBlacklist, providerBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler);

        Throwable t = Assertions.assertThrows(
                RuntimeException.class,
                () -> spendTokenMultipleBlackslists(secondBasketId, storeTwoBlacklist, providerBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler)
        );
        System.out.println(t.getMessage());
        Assertions.assertTrue(t.getMessage().contains("Illegal retry, dsid already used for different request"));
    }

    @Test
    void testSuccessfulLink() {
        ArrayList<SpendTransactionData> spendTransactionData = new ArrayList<>();
        TestSuite.TestDsidBlacklist storeOneBlacklist = new TestSuite.TestDsidBlacklist();
        TestSuite.TestDsidBlacklist storeTwoBlacklist = new TestSuite.TestDsidBlacklist();

        spendTokenStoreOnly(basketId, storeOneBlacklist, pointDifference, pointsAfterSpend, testRedeemedHandler, spendTransactionData::add);
        spendTokenStoreOnly(secondBasketId, storeTwoBlacklist, pointDifferenceAlt, pointsAfterSpendAlt, testRedeemedHandler, spendTransactionData::add);
        SpendTransactionData first = spendTransactionData.get(0);
        SpendTransactionData second = spendTransactionData.get(1);

        DoubleSpendingTag doubleSpendingTag = new DoubleSpendingTag(first.getC(), first.getGamma());
        DoubleSpendingTag secondDoubleSpendingTag = new DoubleSpendingTag(second.getC(), second.getGamma());
        var linkOutput = incSys.link(doubleSpendingTag, secondDoubleSpendingTag);

        Assertions.assertEquals(linkOutput.getDsBlame(), TestSuite.userKeyPair.getSk().getUsk());
        Assertions.assertEquals(linkOutput.getUpk(), TestSuite.userKeyPair.getPk());
    }

    private SpendStoreResponse spendTokenStoreOnly(UUID basketId,
                                                   IDsidBlacklistHandler dsidBlacklistHandler,
                                                   Vector<BigInteger> pointDifference,
                                                   Vector<BigInteger> pointsAfterSpend,
                                                   IStoreBasketRedeemedHandler storeBasketRedeemedHandler,
                                                   ISpendTransactionDBHandler spendTransactionDBHandler) {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendStoreRequest spendStoreRequest = incSys.generateStoreSpendRequest(
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
                spendStoreRequest,
                spendDeductTree,
                TestSuite.context,
                storeBasketRedeemedHandler,
                dsidBlacklistHandler,
                spendTransactionDBHandler
        );
    }

    private SpendProviderResponse spendTokenSyncronizedBlacklists(UUID basketId,
                                                                  IDsidBlacklistHandler dsidBlacklistHandler,
                                                                  Vector<BigInteger> pointDifference,
                                                                  Vector<BigInteger> pointsAfterSpend,
                                                                  IStoreBasketRedeemedHandler storeBasketRedeemedHandler
    ) {
        return spendTokenMultipleBlackslists(basketId,
                dsidBlacklistHandler,
                dsidBlacklistHandler,
                pointDifference,
                pointsAfterSpend,
                storeBasketRedeemedHandler);
    }

    /*
     * For a tests where dsid blacklists are not synchronized immediately
     */
    private SpendProviderResponse spendTokenMultipleBlackslists(UUID basketId,
                                                                IDsidBlacklistHandler storeBlacklist,
                                                                IDsidBlacklistHandler providerBlacklist,
                                                                Vector<BigInteger> pointDifference,
                                                                Vector<BigInteger> pointsAfterSpend,
                                                                IStoreBasketRedeemedHandler testRedeemedHandler) {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendStoreRequest spendStoreRequest = incSys.generateStoreSpendRequest(
                TestSuite.userKeyPair,
                TestSuite.providerKeyPair.getPk(),
                token,
                promotionParameters,
                basketId,
                pointsAfterSpend,
                spendDeductTree,
                TestSuite.context
        );
        SpendStoreResponse spendCouponSignature = incSys.signSpendCoupon(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendStoreRequest,
                spendDeductTree,
                TestSuite.context, testRedeemedHandler,
                storeBlacklist,
                spendTransactionData -> {}
        );
        Assertions.assertTrue(incSys.verifySpendCouponSignature(spendStoreRequest, spendCouponSignature, promotionParameters, basketId));

        SpendProviderRequest spendRequest = new SpendProviderRequest(spendStoreRequest, spendCouponSignature);
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
