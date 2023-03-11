package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.incentive.crypto.exception.StoreDoubleSpendingDetected;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

public class SpendTest {
    final IncentiveSystem incSys = TestSuite.incentiveSystem;
    final UUID basketId = UUID.randomUUID();
    final PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(2);
    final Vector<BigInteger> pointsBeforeSpend = Vector.of(BigInteger.valueOf(10L), BigInteger.valueOf(0L));
    final Vector<BigInteger> pointsAfterSpend = Vector.of(BigInteger.valueOf(6L), BigInteger.valueOf(0L));
    final Vector<BigInteger> pointDifference = pointsBeforeSpend.zip(pointsAfterSpend, BigInteger::subtract);

    final Token token = TestSuite.generateToken(promotionParameters, pointsBeforeSpend);
    final TestRedeemedHandler testRedeemedHandler = new TestRedeemedHandler();
    final IDsidBlacklistHandler dsidBlacklistHandler = new TestSuite.TestDsidBlacklist();

    @Test
    void spendFullTest() {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendStoreRequest spendStoreRequest = incSys.generateStoreSpendRequest(
                TestSuite.userKeyPair, TestSuite.providerKeyPair.getPk(), token,
                promotionParameters, basketId, pointsAfterSpend,
                spendDeductTree,
                TestSuite.context
        );
        SpendStoreResponse spendCouponSignature;
        try {
            spendCouponSignature = incSys.signSpendCoupon(
                    TestSuite.storeKeyPair,
                    TestSuite.providerKeyPair.getPk(),
                    basketId,
                    promotionParameters,
                    spendStoreRequest,
                    spendDeductTree,
                    TestSuite.context,
                    testRedeemedHandler,
                    dsidBlacklistHandler,
                    spendTransactionData -> {}
            );
        } catch (StoreDoubleSpendingDetected e) {
            throw new RuntimeException(e);
        }
        Assertions.assertTrue(incSys.verifySpendCouponSignature(spendStoreRequest, spendCouponSignature, promotionParameters, basketId));

        SpendProviderRequest spendRequest = new SpendProviderRequest(spendStoreRequest, spendCouponSignature);
        SpendProviderResponse spendResponse = incSys.verifySpendRequestAndIssueNewToken(
                TestSuite.providerKeyPair,
                promotionParameters,
                spendRequest,
                basketId,
                spendDeductTree,
                TestSuite.context,
                (z) -> true,
                dsidBlacklistHandler
        );
        Token updatedToken = incSys.retrieveUpdatedTokenFromSpendResponse(TestSuite.userKeyPair, TestSuite.providerKeyPair.getPk(), token, promotionParameters, pointsAfterSpend, spendRequest, spendResponse);
        Assertions.assertEquals(updatedToken.getPoints().map(RingElement::asInteger), pointsAfterSpend);
    }

    @Test
    void representationTests() {
        SpendDeductTree spendDeductTree = SpendHelper.generateSimpleTestSpendDeductTree(promotionParameters, pointDifference);
        SpendStoreRequest spendStoreRequest = incSys.generateStoreSpendRequest(
                TestSuite.userKeyPair, TestSuite.providerKeyPair.getPk(), token,
                promotionParameters, basketId, pointsAfterSpend,
                spendDeductTree,
                TestSuite.context
        );
        ArrayList<SpendTransactionData> spendTxData = new ArrayList<>();
        SpendStoreResponse spendCouponSignature;
        try {
            spendCouponSignature = incSys.signSpendCoupon(
                    TestSuite.storeKeyPair,
                    TestSuite.providerKeyPair.getPk(),
                    basketId,
                    promotionParameters,
                    spendStoreRequest,
                    spendDeductTree,
                    TestSuite.context, testRedeemedHandler,
                    dsidBlacklistHandler,
                    spendTxData::add
            );
        } catch (StoreDoubleSpendingDetected e) {
            throw new RuntimeException(e);
        }

        SpendTransactionData spendTransactionData = spendTxData.get(0);
        SpendProviderRequest spendRequest = new SpendProviderRequest(spendStoreRequest, spendCouponSignature);
        SpendProviderResponse spendResponse = incSys.verifySpendRequestAndIssueNewToken(
                TestSuite.providerKeyPair,
                promotionParameters,
                spendRequest,
                basketId,
                spendDeductTree,
                TestSuite.context,
                (z) -> true,
                new TestSuite.TestDsidBlacklist()
        );

        SpendStoreResponse deserializedSpendCouponSignature = new SpendStoreResponse(spendCouponSignature.getRepresentation());
        SpendTransactionData deserializedSpendTransactionData = new SpendTransactionData(spendTransactionData.getRepresentation(),
                incSys.pp,
                promotionParameters,
                spendDeductTree,
                TestSuite.providerKeyPair.getPk(),
                TestSuite.context);
        SpendStoreRequest deserialzedSpendStoreRequest = new SpendStoreRequest(
                spendStoreRequest.getRepresentation(),
                incSys.pp,
                basketId,
                promotionParameters,
                TestSuite.providerKeyPair.getPk(),
                spendDeductTree,
                TestSuite.context
        );
        SpendProviderRequest deserializedSpendRequest = new SpendProviderRequest(spendRequest.getRepresentation(),
                incSys.pp,
                promotionParameters,
                basketId,
                spendDeductTree,
                TestSuite.providerKeyPair.getPk(),
                TestSuite.context);
        SpendProviderResponse deserializedSpendResponse = new SpendProviderResponse(spendResponse.getRepresentation(), incSys.pp);

        Assertions.assertEquals(spendStoreRequest, deserialzedSpendStoreRequest);
        Assertions.assertEquals(spendCouponSignature, deserializedSpendCouponSignature);
        Assertions.assertEquals(spendTransactionData, deserializedSpendTransactionData);
        Assertions.assertEquals(spendRequest, deserializedSpendRequest);
        Assertions.assertEquals(spendResponse, deserializedSpendResponse);
    }

}
