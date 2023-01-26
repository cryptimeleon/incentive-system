package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.math.structures.cartesian.Vector;
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
    Vector<BigInteger> earnAmount = Vector.of(BigInteger.valueOf(3L), BigInteger.valueOf(5L));

    @Test
    public void earnTest() {
        var earnAmount = Vector.of(BigInteger.valueOf(3L), BigInteger.valueOf(5L));

        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair, basketId, promotionId);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, (UUID basketId, BigInteger promotionId, byte[] hash) -> true);
        assertThat(incSys.verifyEarnCoupon(TestSuite.storeKeyPair.getPk(), promotionParameters.getPromotionId(), earnAmount, storeReq, storeRes))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, promotionParameters.getPromotionId(), earnAmount, storeRes);
        var providerRes = incSys.generateEarnResponse(promotionParameters, TestSuite.providerKeyPair, providerReq, earnAmount, (a, b, c, d) -> {});

        var updatedToken = incSys.handleEarnResponse(promotionParameters, providerReq, providerRes, earnAmount, token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair);

        assertThat(updatedToken.getPoints().zip(earnAmount, (l, r) -> l.asInteger().equals(r)).reduce((l, r) -> l && r))
                .isTrue();
    }

    @Test
    public void earnTestManipulatedEarnAmountFails() {
        var invalidEarnAmount = Vector.of(BigInteger.valueOf(5L), BigInteger.valueOf(7L));
        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair, basketId, promotionId);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, (UUID basketId, BigInteger promotionId, byte[] hash) -> true);
        assertThat(incSys.verifyEarnCoupon(TestSuite.storeKeyPair.getPk(), promotionParameters.getPromotionId(), earnAmount, storeReq, storeRes))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(token, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, promotionParameters.getPromotionId(), invalidEarnAmount, storeRes);

        assertThatThrownBy(() -> incSys.generateEarnResponse(promotionParameters, TestSuite.providerKeyPair, providerReq, earnAmount, (a, b, c, d) -> {}))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void earnTestInvalidHashStoreSided() {
        var storeReq = incSys.generateEarnCouponRequest(token, TestSuite.userKeyPair, basketId, promotionId);

        // Set lambda to false => hash invalid
        assertThatThrownBy(() -> incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, (UUID basketId, BigInteger promotionId, byte[] hash) -> false))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void earnTestInvalidSPSEQForToken() {
        var tokenWithDoubledPoints = new Token(
                token.getCommitment0().pow(2),
                token.getCommitment1(),
                token.getEncryptionSecretKey(),
                token.getDoubleSpendRandomness0(),
                token.getDoubleSpendRandomness1(),
                token.getZ(),
                token.getT(),
                token.getPromotionId(),
                token.getPoints(),
                token.getSignature()
        );

        var storeReq = incSys.generateEarnCouponRequest(tokenWithDoubledPoints, TestSuite.userKeyPair, basketId, promotionId);
        var storeRes = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, (UUID basketId, BigInteger promotionId, byte[] hash) -> true);
        assertThat(incSys.verifyEarnCoupon(TestSuite.storeKeyPair.getPk(), promotionParameters.getPromotionId(), earnAmount, storeReq, storeRes))
                .isTrue();

        var providerReq = incSys.generateEarnRequest(tokenWithDoubledPoints, TestSuite.providerKeyPair.getPk(), TestSuite.userKeyPair, promotionParameters.getPromotionId(), earnAmount, storeRes);
        assertThatThrownBy(() -> incSys.generateEarnResponse(promotionParameters, TestSuite.providerKeyPair, providerReq, earnAmount, (a, b, c, d) -> {}))
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
        EarnStoreCoupon earnStoreCoupon = incSys.signEarnCoupon(TestSuite.storeKeyPair, earnAmount, storeReq, (UUID basketId, BigInteger promotionId, byte[] hash) -> true);

        EarnStoreCoupon recoveredEarnStoreCoupon = new EarnStoreCoupon(earnStoreCoupon.getRepresentation());

        assertThat(recoveredEarnStoreCoupon).isEqualTo(earnStoreCoupon);
    }
}
