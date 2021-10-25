package org.cryptimeleon.incentive.crypto.proof.spend;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TokenUpdateProofTest {

    private static final int TEST_VECTOR_SIZE = 3;
    BigInteger[] points = {
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
    };
    BigInteger[] newPoints = {
            BigInteger.valueOf(2),
            BigInteger.valueOf(3),
            BigInteger.valueOf(4),
    };
    BigInteger[] aVector = {
            BigInteger.valueOf(1),
            BigInteger.valueOf(1),
            BigInteger.valueOf(0),
    };
    BigInteger[] bVector = {
            BigInteger.valueOf(-1),
            BigInteger.valueOf(0),
            BigInteger.valueOf(4),
    };
    BigInteger[] invalidBVector = {
            BigInteger.valueOf(-1),
            BigInteger.valueOf(1),
            BigInteger.valueOf(4),
    };
    BigInteger[] tooSmallNewPoints = {
            BigInteger.valueOf(-1),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3)
    };
    BigInteger[] lowerLimitsZero = {
            BigInteger.valueOf(0),
            BigInteger.valueOf(0),
            BigInteger.valueOf(0)
    };
    BigInteger[] upperLimits = {
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(4)
    };
    BigInteger[] ignoreVector = {null, null, null};

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
        pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        zn = pp.getBg().getZn();
        providerKey = Setup.providerKeyGen(pp);
        userKey = Setup.userKeyGen(pp);
        incentiveSystem = new IncentiveSystem(pp);
        promotion = incentiveSystem.generatePromotionParameters(TEST_VECTOR_SIZE);
        token = Helper.generateToken(pp, userKey, providerKey, promotion, Vector.fromStreamPlain(Arrays.stream(points)));

        var testSuite = SpendHelper.generateTestSuite(
                newPoints,
                pp,
                promotion,
                providerKey,
                token,
                userKey,
                zn
        );

        witness = testSuite.witness;
        commonInput = testSuite.commonInput;
    }

    @Test
    void validIgnoreTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateProof(pp, ignoreVector, ignoreVector, ignoreVector, ignoreVector, providerKey.getPk(), promotion));
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        assertTrue(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void validLinearTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateProof(pp, ignoreVector, ignoreVector, aVector, bVector, providerKey.getPk(), promotion));
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        assertTrue(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void invalidLinearTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateProof(pp, ignoreVector, ignoreVector, aVector, invalidBVector, providerKey.getPk(), promotion));

        var proof = fiatShamirProofSystem.createProof(commonInput, witness);
        assertFalse(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void validRangeTest() {
        // Do whatever you want within the range :)
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateProof(pp, lowerLimitsZero, upperLimits, ignoreVector, ignoreVector, providerKey.getPk(), promotion));
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        assertTrue(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void invalidRangeTest() {
        // need other witness for this test
        var testSuite = SpendHelper.generateTestSuite(
                tooSmallNewPoints,
                pp,
                promotion,
                providerKey,
                token,
                userKey,
                zn
        );

        witness = testSuite.witness;
        commonInput = testSuite.commonInput;
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateProof(pp, lowerLimitsZero, ignoreVector, ignoreVector, ignoreVector, providerKey.getPk(), promotion));

        assertThrows(RuntimeException.class, () -> {
            var proof = fiatShamirProofSystem.createProof(commonInput, witness);
            fiatShamirProofSystem.checkProof(commonInput, proof);
        });
    }
}