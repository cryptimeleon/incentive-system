package org.cryptimeleon.incentive.crypto.proof;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendDeductZkpWitnessInput;
import org.cryptimeleon.incentive.crypto.proof.spend.TokenPointsRangeProof;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenPointsRangeProofTest {

    BigInteger[] points = {
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3)
    };
    BigInteger[] lowerLimits = {
            BigInteger.valueOf(2),
            BigInteger.valueOf(3),
            null,
            null
    };
    BigInteger[] upperLimits = {
            BigInteger.valueOf(4),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            null
    };

    BigInteger[] invalidLowerLimits = {
            BigInteger.valueOf(4),
            null,
            null,
            null
    };
    BigInteger[] invalidUpperLimits = {
            BigInteger.valueOf(2),
            null,
            null,
            null
    };

    IncentivePublicParameters pp;
    ProviderKeyPair providerKey;
    UserKeyPair userKey;
    IncentiveSystem incentiveSystem;
    PromotionParameters promotion;
    Token token;
    Zn zn;
    SpendDeductZkpWitnessInput witness;
    SpendDeductZkpCommonInput commonInput;
    Zn.ZnElement gamma;

    @BeforeEach
    void setup() {
        pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        providerKey = Setup.providerKeyGen(pp);
        userKey = Setup.userKeyGen(pp);
        incentiveSystem = new IncentiveSystem(pp);
        promotion = incentiveSystem.generatePromotionParameters(4);
        token = Helper.generateToken(pp, userKey, providerKey, promotion, Vector.fromStreamPlain(Arrays.stream(points)));

        zn = pp.getBg().getZn();

        witness = new SpendDeductZkpWitnessInput(
                token,
                userKey.getSk().getUsk(),
                zn.getUniformlyRandomElement(),
                zn.getUniformlyRandomElement(),
                zn.getUniformlyRandomElement(),
                zn.getUniformlyRandomElement(),
                zn.getUniformlyRandomElement(),
                zn.getUniformlyRandomElement(),
                RingElementVector.of(zn.getUniformlyRandomElement()),
                RingElementVector.of(zn.getUniformlyRandomElement()),
                RingElementVector.of(zn.getUniformlyRandomElement())
        );
        gamma = zn.getUniformlyRandomElement();
        commonInput = new SpendDeductZkpCommonInput(
                gamma,
                zn.getUniformlyRandomElement(),
                zn.getUniformlyRandomElement(),
                pp.getBg().getG1().getUniformlyRandomElement(),
                pp.getBg().getG1().getUniformlyRandomElement(),
                pp.getBg().getG1().getUniformlyRandomElement(),
                token.getCommitment0(),
                GroupElementVector.of(pp.getBg().getG1().getUniformlyRandomElement()),
                GroupElementVector.of(pp.getBg().getG1().getUniformlyRandomElement())
        );
    }

    @Test
    void validBoundariesTest() {

        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenPointsRangeProof(pp, lowerLimits, upperLimits, providerKey.getPk(), promotion));
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        assertTrue(fiatShamirProofSystem.checkProof(commonInput, proof));
    }

    @Test
    void invalidUpperLimitsTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenPointsRangeProof(pp, lowerLimits, invalidUpperLimits, providerKey.getPk(), promotion));

        assertThrows(RuntimeException.class, () -> {
            var proof = fiatShamirProofSystem.createProof(commonInput, witness);
            fiatShamirProofSystem.checkProof(commonInput, proof);
        });
    }

    @Test
    void invalidLowerLimitsTest() {
        var fiatShamirProofSystem = new FiatShamirProofSystem(new TokenPointsRangeProof(pp, invalidLowerLimits, upperLimits, providerKey.getPk(), promotion));

        assertThrows(RuntimeException.class, () -> {
            var proof = fiatShamirProofSystem.createProof(commonInput, witness);
            fiatShamirProofSystem.checkProof(commonInput, proof);
        });
    }
}