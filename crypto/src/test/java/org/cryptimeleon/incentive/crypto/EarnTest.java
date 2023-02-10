package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EarnTest {

    IncentiveSystem incSys = TestSuite.incentiveSystem;
    UUID basketId = UUID.randomUUID();
    PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(2);
    BigInteger promotionId = promotionParameters.getPromotionId();
    Token token = TestSuite.generateToken(promotionParameters);
    Token altToken = TestSuite.generateToken(promotionParameters);
    Vector<BigInteger> earnAmount = Vector.of(BigInteger.valueOf(3L), BigInteger.valueOf(5L));
    TestRedeemedHandler testRedeemedHandler;

    @BeforeEach
    void setup() {
        testRedeemedHandler = new TestRedeemedHandler();
    }

    @Test
    public void earnTest() {
        var earnAmount = Vector.of(BigInteger.valueOf(3L), BigInteger.valueOf(5L));

        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair, basketId, promotionId);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, testRedeemedHandler);
        assertThat(incSys.verifyEarnCoupon(storeReq, earnAmount, storeRes, storePublicKey -> true))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, promotionParameters.getPromotionId(), earnAmount, storeRes);
        var providerRes = incSys.generateEarnResponse(providerReq, promotionParameters, TestSuite.providerKeyPair, (a, b) -> {
        }, (a) -> true);

        var updatedToken = incSys.handleEarnResponse(providerReq, providerRes, promotionParameters, token, TestSuite.userKeyPair, TestSuite.providerKeyPair.getPk());

        assertThat(updatedToken.getPoints().zip(earnAmount, (l, r) -> l.asInteger().equals(r)).reduce((l, r) -> l && r))
                .isTrue();
    }

    @Test
    public void earnTestManipulatedEarnAmountFails() {
        var invalidEarnAmount = Vector.of(BigInteger.valueOf(5L), BigInteger.valueOf(7L));

        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair, basketId, promotionId);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, testRedeemedHandler);
        assertThat(incSys.verifyEarnCoupon(storeReq, earnAmount, storeRes, storePublicKey -> true))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, promotionParameters.getPromotionId(), invalidEarnAmount, storeRes);

        assertThatThrownBy(() -> incSys.generateEarnResponse(providerReq, promotionParameters, TestSuite.providerKeyPair, (a, b) -> {
        }, (a) -> true))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void earnTestInvalidHashStoreSided() {
        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair, basketId, promotionId);
        incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, testRedeemedHandler);

        // Try to earn again for basket and promotion
        var storeReq2 = incSys.generateEarnCouponRequest(altToken, TestSuite.userKeyPair, basketId, promotionId);
        // Set lambda to false => hash invalid
        assertThatThrownBy(() -> incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq2, testRedeemedHandler))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void earnTestInvalidSPSEQForToken() {
        var tokenWithDoubledPoints = new Token(
                token.getCommitment0().pow(2),
                token.getCommitment1(),
                token.getDoubleSpendingId(),
                token.getDoubleSpendRandomness(),
                token.getZ(),
                token.getT(),
                token.getPromotionId(),
                token.getPoints(),
                token.getSignature()
        );

        var storeReq = incSys.generateEarnCouponRequest(tokenWithDoubledPoints, TestSuite.userKeyPair, basketId, promotionId);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, testRedeemedHandler);
        assertThat(incSys.verifyEarnCoupon(storeReq, earnAmount, storeRes, storePublicKey -> true))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(tokenWithDoubledPoints, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, promotionParameters.getPromotionId(), earnAmount, storeRes);
        assertThatThrownBy(() -> incSys.generateEarnResponse(providerReq, promotionParameters, TestSuite.providerKeyPair, (a, b) -> {
        }, (a) -> true))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void earnStoreRequestRepresentationTest() {
        EarnStoreRequest earnStoreRequest = new EarnStoreRequest("Test".getBytes(), basketId, promotionId);

        EarnStoreRequest recoveredEarnStoreRequest = new EarnStoreRequest(earnStoreRequest.getRepresentation());

        assertThat(recoveredEarnStoreRequest).isEqualTo(earnStoreRequest);
    }

    @Test
    void earnStoreCouponRepresentationTest() {
        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair, basketId, promotionId);
        EarnStoreCouponSignature earnStoreCouponSignature = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, testRedeemedHandler);

        EarnStoreCouponSignature recoveredEarnStoreCouponSignature = new EarnStoreCouponSignature(earnStoreCouponSignature.getRepresentation());

        assertThat(recoveredEarnStoreCouponSignature).isEqualTo(earnStoreCouponSignature);
    }

    @Test
    void earnRequestECDSARepresentationTest() {
        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair, basketId, promotionId);
        EarnStoreCouponSignature earnStoreCouponSignature = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, testRedeemedHandler);
        EarnRequestECDSA earnRequestECDSA = incSys.generateEarnRequest(token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, promotionId, Vector.of(BigInteger.ONE), earnStoreCouponSignature);

        EarnRequestECDSA deserializedEarnRequestECDSA = new EarnRequestECDSA(earnRequestECDSA.getRepresentation(), TestSuite.pp);

        assertThat(deserializedEarnRequestECDSA).isEqualTo(earnRequestECDSA);
    }
}
