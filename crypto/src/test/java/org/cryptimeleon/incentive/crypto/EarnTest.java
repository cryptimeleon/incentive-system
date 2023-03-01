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

    final IncentiveSystem incSys = TestSuite.incentiveSystem;
    final UUID basketId = UUID.randomUUID();
    final PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(2);
    final BigInteger promotionId = promotionParameters.getPromotionId();
    final Token token = TestSuite.generateToken(promotionParameters);
    final Token altToken = TestSuite.generateToken(promotionParameters);
    final Vector<BigInteger> earnAmount = Vector.of(BigInteger.valueOf(3L), BigInteger.valueOf(5L));
    TestRedeemedHandler testRedeemedHandler;
    TestSuite.TestTransactionDbHandler transactionDbHandler;

    @BeforeEach
    void setup() {
        testRedeemedHandler = new TestRedeemedHandler();
        transactionDbHandler = new TestSuite.TestTransactionDbHandler();
    }

    @Test
    public void earnTest() {
        var earnAmount = Vector.of(BigInteger.valueOf(3L), BigInteger.valueOf(5L));

        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, basketId, promotionId, testRedeemedHandler);
        assertThat(incSys.verifyEarnCoupon(storeReq, promotionId, earnAmount, storeRes, storePublicKey -> true))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, earnAmount, storeRes);
        var providerRes = incSys.generateEarnResponse(providerReq, promotionParameters, TestSuite.providerKeyPair, transactionDbHandler, (a) -> true);

        var updatedToken = incSys.handleEarnResponse(providerReq, providerRes, promotionParameters, token, TestSuite.userKeyPair, TestSuite.providerKeyPair.getPk());

        assertThat(updatedToken.getPoints().zip(earnAmount, (l, r) -> l.asInteger().equals(r)).reduce((l, r) -> l && r))
                .isTrue();
    }

    @Test
    public void earnTestManipulatedEarnAmountFails() {
        var invalidEarnAmount = Vector.of(BigInteger.valueOf(5L), BigInteger.valueOf(7L));

        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, basketId, promotionId, testRedeemedHandler);
        assertThat(incSys.verifyEarnCoupon(storeReq, promotionId, earnAmount, storeRes, storePublicKey -> true))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, invalidEarnAmount, storeRes);

        assertThatThrownBy(() -> incSys.generateEarnResponse(providerReq, promotionParameters, TestSuite.providerKeyPair, transactionDbHandler, (a) -> true))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void earnTestInvalidHashStoreSided() {
        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair);
        incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, basketId, promotionId, testRedeemedHandler);

        // Try to earn again for basket and promotion
        var storeReq2 = incSys.generateEarnCouponRequest(altToken, TestSuite.userKeyPair);
        // Set lambda to false => hash invalid
        assertThatThrownBy(() -> incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq2, basketId, promotionId, testRedeemedHandler))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void earnTestInvalidSPSEQForToken() {
        var tokenWithDoubledPoints = new Token(
                token.getCommitment0().pow(2),
                token.getDoubleSpendingId(),
                token.getDoubleSpendRandomness(),
                token.getZ(),
                token.getT(),
                token.getPromotionId(),
                token.getPoints(),
                token.getSignature()
        );

        var storeReq = incSys.generateEarnCouponRequest(tokenWithDoubledPoints, TestSuite.userKeyPair);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, basketId, promotionId, testRedeemedHandler);
        assertThat(incSys.verifyEarnCoupon(storeReq, promotionId, earnAmount, storeRes, storePublicKey -> true))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(tokenWithDoubledPoints, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, earnAmount, storeRes);
        assertThatThrownBy(() -> incSys.generateEarnResponse(providerReq, promotionParameters, TestSuite.providerKeyPair, transactionDbHandler, (a) -> true))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void earnStoreRequestRepresentationTest() {
        EarnStoreRequest earnStoreRequest = new EarnStoreRequest("Test".getBytes());

        EarnStoreRequest recoveredEarnStoreRequest = new EarnStoreRequest(earnStoreRequest.getRepresentation());

        assertThat(recoveredEarnStoreRequest).isEqualTo(earnStoreRequest);
    }

    @Test
    void earnStoreCouponRepresentationTest() {
        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair);
        EarnStoreCouponSignature earnStoreCouponSignature = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, basketId, promotionId, testRedeemedHandler);

        EarnStoreCouponSignature recoveredEarnStoreCouponSignature = new EarnStoreCouponSignature(earnStoreCouponSignature.getRepresentation());

        assertThat(recoveredEarnStoreCouponSignature).isEqualTo(earnStoreCouponSignature);
    }

    @Test
    void earnRequestECDSARepresentationTest() {
        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair);
        EarnStoreCouponSignature earnStoreCouponSignature = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, basketId, promotionId, testRedeemedHandler);
        EarnRequestECDSA earnRequestECDSA = incSys.generateEarnRequest(token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, Vector.of(BigInteger.ONE), earnStoreCouponSignature);

        EarnRequestECDSA deserializedEarnRequestECDSA = new EarnRequestECDSA(earnRequestECDSA.getRepresentation(), TestSuite.pp);

        assertThat(deserializedEarnRequestECDSA).isEqualTo(earnRequestECDSA);
    }
}
