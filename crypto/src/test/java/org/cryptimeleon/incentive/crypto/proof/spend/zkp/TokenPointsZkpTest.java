package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendHelper;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenPointsZkpTest {

    BigInteger[] points = {
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3)
    };
    Vector<BigInteger> lowerLimits = Vector.of(
            BigInteger.valueOf(2),
            BigInteger.valueOf(3),
            null,
            null
    );
    Vector<BigInteger> upperLimits = Vector.of(
            BigInteger.valueOf(4),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            null
    );

    Vector<BigInteger> invalidLowerLimits = Vector.of(
            BigInteger.valueOf(4),
            null,
            null,
            null
    );
    Vector<BigInteger> invalidUpperLimits = Vector.of(
            BigInteger.valueOf(2),
            null,
            null,
            null
    );

    IncentivePublicParameters pp;
    ProviderKeyPair providerKey;
    UserKeyPair userKey;
    IncentiveSystem incentiveSystem;
    PromotionParameters promotion;
    Token token;
    Zn zn;
    SpendDeductZkpWitnessInput witness;
    SpendDeductZkpCommonInput commonInput;

    @BeforeEach
    void setup() {
        pp = TestSuite.pp;
        providerKey = TestSuite.providerKeyPair;
        userKey = TestSuite.userKeyPair;
        incentiveSystem = TestSuite.incentiveSystem;
        promotion = IncentiveSystem.generatePromotionParameters(4);
        token = TestSuite.generateToken(promotion, Vector.fromStreamPlain(Arrays.stream(points)));

        zn = pp.getBg().getZn();
        var testSuite = SpendHelper.generateTestSuite(
                Util.getZeroBigIntegerVector(4), pp, promotion, providerKey, token, userKey
        );

        witness = testSuite.witness;
        commonInput = testSuite.commonInput;
    }

    @Test
    void validBoundariesTest() {

        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenPointsZkp(pp, lowerLimits, upperLimits, providerKey.getPk(), promotion));
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        assertTrue(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void invalidUpperLimitsTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenPointsZkp(pp, lowerLimits, invalidUpperLimits, providerKey.getPk(), promotion));

        assertThrows(RuntimeException.class, () -> {
            var proof = fiatShamirProofSystem.createProof(commonInput, witness);
            fiatShamirProofSystem.checkProof(commonInput, proof);
        });
    }

    @Test
    void invalidLowerLimitsTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenPointsZkp(pp, invalidLowerLimits, upperLimits, providerKey.getPk(), promotion));

        assertThrows(RuntimeException.class, () -> {
            var proof = fiatShamirProofSystem.createProof(commonInput, witness);
            fiatShamirProofSystem.checkProof(commonInput, proof);
        });
    }
}
