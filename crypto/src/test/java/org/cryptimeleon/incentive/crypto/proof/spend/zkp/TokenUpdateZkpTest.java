package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.incentive.crypto.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.*;
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

import static org.junit.jupiter.api.Assertions.*;

class TokenUpdateZkpTest {

    private static final int TEST_VECTOR_SIZE = 3;
    Vector<BigInteger> points = Vector.of(
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3)
    );
    Vector<BigInteger> newPoints = Vector.of(
            BigInteger.valueOf(2),
            BigInteger.valueOf(3),
            BigInteger.valueOf(4)
    );
    Vector<BigInteger> aVector = Vector.of(
            BigInteger.valueOf(1),
            BigInteger.valueOf(1),
            BigInteger.valueOf(0)
    );
    Vector<BigInteger> bVector = Vector.of(
            BigInteger.valueOf(-1),
            BigInteger.valueOf(0),
            BigInteger.valueOf(4)
    );
    Vector<BigInteger> invalidBVector = Vector.of(
            BigInteger.valueOf(-1),
            BigInteger.valueOf(1),
            BigInteger.valueOf(4)
    );
    Vector<BigInteger> tooSmallNewPoints = Vector.of(
            BigInteger.valueOf(-1),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3)
    );
    Vector<BigInteger> lowerLimitsZero = Util.getZeroBigIntegerVector(TEST_VECTOR_SIZE);
    Vector<BigInteger> upperLimits = Vector.of(
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(4)
    );
    Vector<BigInteger> ignoreVector = Util.getNullBigIntegerVector(TEST_VECTOR_SIZE);

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
        zn = pp.getBg().getZn();
        providerKey = TestSuite.providerKeyPair;
        userKey = TestSuite.userKeyPair;
        incentiveSystem = TestSuite.incentiveSystem;
        promotion = IncentiveSystem.generatePromotionParameters(TEST_VECTOR_SIZE);
        token = TestSuite.generateToken(promotion, points);

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
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateZkp(pp, ignoreVector, ignoreVector, ignoreVector, ignoreVector, providerKey.getPk(), promotion));
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        assertTrue(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void validLinearTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateZkp(pp, ignoreVector, ignoreVector, aVector, bVector, providerKey.getPk(), promotion));
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        assertTrue(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void invalidLinearTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateZkp(pp, ignoreVector, ignoreVector, aVector, invalidBVector, providerKey.getPk(), promotion));

        var proof = fiatShamirProofSystem.createProof(commonInput, witness);
        assertFalse(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void validRangeTest() {
        // Do whatever you want within the range :)
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateZkp(pp, lowerLimitsZero, upperLimits, ignoreVector, ignoreVector, providerKey.getPk(), promotion));
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
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenUpdateZkp(pp, lowerLimitsZero, ignoreVector, ignoreVector, ignoreVector, providerKey.getPk(), promotion));

        assertThrows(RuntimeException.class, () -> {
            var proof = fiatShamirProofSystem.createProof(commonInput, witness);
            fiatShamirProofSystem.checkProof(commonInput, proof);
        });
    }
}
