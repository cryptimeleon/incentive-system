package org.cryptimeleon.incentive.crypto;

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
        SpendStoreOutput spendStoreOutput = incSys.signSpendCoupon(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendCouponRequest,
                spendDeductTree,
                testRedeemedHandler,
                new TestSuite.TestDsidBlacklist(),
                TestSuite.context
        );
        SpendCouponSignature spendCouponSignature = spendStoreOutput.spendCouponSignature;
        Assertions.assertTrue(incSys.verifySpendCouponSignature(spendCouponRequest, spendCouponSignature, promotionParameters, basketId));

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
        SpendStoreOutput spendStoreOutput = incSys.signSpendCoupon(
                TestSuite.storeKeyPair,
                TestSuite.providerKeyPair.getPk(),
                basketId,
                promotionParameters,
                spendCouponRequest,
                spendDeductTree,
                new TestRedeemedHandler(),
                new TestSuite.TestDsidBlacklist(),
                TestSuite.context
        );

        SpendCouponSignature spendCouponSignature = spendStoreOutput.spendCouponSignature;
        SpendClearingData spendClearingData = spendStoreOutput.spendClearingData;

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
        SpendClearingData deserializedSpendClearingData = new SpendClearingData(spendClearingData.getRepresentation(),
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
        Assertions.assertEquals(spendClearingData, deserializedSpendClearingData);
        Assertions.assertEquals(spendRequest, deserializedSpendRequest);
        Assertions.assertEquals(spendResponse, deserializedSpendResponse);
    }

}
