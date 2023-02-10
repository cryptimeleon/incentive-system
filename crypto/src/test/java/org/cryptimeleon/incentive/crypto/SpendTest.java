package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.UUID;

public class SpendTest {
    IncentiveSystem incSys = TestSuite.incentiveSystem;
    UUID basketId = UUID.randomUUID();
    PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(2);
    Vector<BigInteger> pointsBeforeSpend = Vector.of(BigInteger.valueOf(10L), BigInteger.valueOf(0L));
    Vector<BigInteger> pointsAfterSpend = Vector.of(BigInteger.valueOf(6L), BigInteger.valueOf(0L));
    Vector<BigInteger> pointDifference = pointsBeforeSpend.zip(pointsAfterSpend, BigInteger::subtract);

    Token token = TestSuite.generateToken(promotionParameters, pointsBeforeSpend);
    TestRedeemedHandler testRedeemedHandler = new TestRedeemedHandler();
    IDsidBlacklistHandler dsidBlacklistHandler = new TestSuite.TestDsidBlacklist();
    TestSuite.TestTransactionDbHandler transactionDbHandler = new TestSuite.TestTransactionDbHandler();

    @Test
    void spendFullTest() {
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
        SpendCouponSignature spendCouponSignature = incSys.signSpendCoupon(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendCouponRequest,
                spendDeductTree,
                testRedeemedHandler,
                dsidBlacklistHandler,
                transactionDbHandler,
                TestSuite.context
        );
        Assertions.assertTrue(incSys.verifySpendCouponSignature(spendCouponRequest, spendCouponSignature, promotionParameters, basketId));

        SpendRequestECDSA spendRequest = new SpendRequestECDSA(spendCouponRequest, spendCouponSignature, promotionParameters, basketId);
        SpendResponseECDSA spendResponse = incSys.verifySpendRequestAndIssueNewToken(
                TestSuite.providerKeyPair,
                spendRequest,
                promotionParameters,
                (z) -> true,
                dsidBlacklistHandler,
                spendDeductTree,
                TestSuite.context
        );
        Token updatedToken = incSys.retrieveUpdatedTokenFromSpendResponse(spendRequest, spendResponse, pointsAfterSpend, TestSuite.userKeyPair, token, TestSuite.providerKeyPair.getPk(), promotionParameters);
        Assertions.assertEquals(updatedToken.getPoints().map(RingElement::asInteger), pointsAfterSpend);
    }

    @Test
    void representationTests() {
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
        SpendCouponSignature spendCouponSignature = incSys.signSpendCoupon(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendCouponRequest,
                spendDeductTree,
                testRedeemedHandler,
                dsidBlacklistHandler,
                transactionDbHandler,
                TestSuite.context
        );

        SpendTransactionData spendTransactionData = transactionDbHandler.spendData.get(0);
        SpendRequestECDSA spendRequest = new SpendRequestECDSA(spendCouponRequest, spendCouponSignature, promotionParameters, basketId);
        SpendResponseECDSA spendResponse = incSys.verifySpendRequestAndIssueNewToken(
                TestSuite.providerKeyPair,
                spendRequest,
                promotionParameters,
                (z) -> true,
                new TestSuite.TestDsidBlacklist(),
                spendDeductTree,
                TestSuite.context
        );

        SpendCouponSignature deserializedSpendCouponSignature = new SpendCouponSignature(spendCouponSignature.getRepresentation());
        SpendTransactionData deserializedSpendTransactionData = new SpendTransactionData(spendTransactionData.getRepresentation(),
                incSys.pp,
                promotionParameters,
                spendDeductTree,
                TestSuite.providerKeyPair.getPk(),
                TestSuite.context);
        SpendCouponRequest deserialzedSpendCouponRequest = new SpendCouponRequest(
                spendCouponRequest.getRepresentation(),
                incSys.pp,
                basketId,
                promotionParameters,
                TestSuite.providerKeyPair.getPk(),
                spendDeductTree,
                TestSuite.context
        );
        SpendRequestECDSA deserializedSpendRequest = new SpendRequestECDSA(spendRequest.getRepresentation(),
                incSys.pp,
                promotionParameters,
                spendDeductTree,
                TestSuite.providerKeyPair.getPk(),
                TestSuite.context);
        SpendResponseECDSA deserializedSpendResponse = new SpendResponseECDSA(spendResponse.getRepresentation(), incSys.pp);

        Assertions.assertEquals(spendCouponRequest, deserialzedSpendCouponRequest);
        Assertions.assertEquals(spendCouponSignature, deserializedSpendCouponSignature);
        Assertions.assertEquals(spendTransactionData, deserializedSpendTransactionData);
        Assertions.assertEquals(spendRequest, deserializedSpendRequest);
        Assertions.assertEquals(spendResponse, deserializedSpendResponse);
    }

}
