package org.cryptimeleon.incentive.crypto.proof.spend;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.MetadataLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductOrNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpendDeductBooleanZkpTest {

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
    BigInteger[] invalidUpperLimits = {
            BigInteger.valueOf(2),
            null,
            null,
            null
    };
    BigInteger[] newPoints = {
            BigInteger.valueOf(2),
            BigInteger.valueOf(1),
            BigInteger.valueOf(0),
            BigInteger.valueOf(3),
    };

    IncentivePublicParameters pp;
    ProviderKeyPair providerKey;
    UserKeyPair userKey;
    IncentiveSystem incentiveSystem;
    PromotionParameters promotion;
    Token token;
    Zn zn;
    SpendHelper.SpendZkpTestSuite testSuite;

    @BeforeEach
    void setup() {
        pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        providerKey = Setup.providerKeyGen(pp);
        userKey = Setup.userKeyGen(pp);
        incentiveSystem = new IncentiveSystem(pp);
        promotion = incentiveSystem.generatePromotionParameters(4);
        token = Helper.generateToken(pp, userKey, providerKey, promotion, Vector.fromStreamPlain(Arrays.stream(points)));
        zn = pp.getBg().getZn();
        testSuite = SpendHelper.generateTestSuite(newPoints, pp, promotion, providerKey, token, userKey, zn);
    }

    @Test
    void testClassicLeaf() {
        SpendDeductTree proofTree = new MetadataLeaf();
        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(proofTree, pp, promotion, providerKey.getPk());

        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));
    }

    @Test
    void testRangeProofLeaf() {
        SpendDeductTree proofTree = new TokenPointsLeaf("RangeProof", lowerLimits, upperLimits, true);
        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(proofTree, pp, promotion, providerKey.getPk());

        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));
    }

    @Test
    void testFalseRangeProofLeaf() {
        SpendDeductTree proofTree = new TokenPointsLeaf("RangeProof", lowerLimits, invalidUpperLimits, false);
        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(proofTree, pp, promotion, providerKey.getPk());

        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        Assertions.assertThrows(RuntimeException.class, () -> {
            FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
            fiatShamirProofSystem.checkProof(testSuite.commonInput, proof);
        });
    }

    @Test
    void testAnd() {
        SpendDeductTree firstValidLeaf = new TokenPointsLeaf("RangeProof1", lowerLimits, upperLimits, true);
        SpendDeductTree secondValidLeaf = new TokenPointsLeaf("RangeProof2", lowerLimits, upperLimits, true);
        SpendDeductTree invalidLeaf = new TokenPointsLeaf("RangeProof3", lowerLimits, invalidUpperLimits, false);

        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(new SpendDeductAndNode(firstValidLeaf, secondValidLeaf), pp, promotion, providerKey.getPk());
        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));

        Assertions.assertThrows(RuntimeException.class, () -> {
            SpendDeductBooleanZkp invalidSigmaProtocol = new SpendDeductBooleanZkp(new SpendDeductAndNode(firstValidLeaf, invalidLeaf), pp, promotion, providerKey.getPk());
            FiatShamirProofSystem invalidFiatShamirProofSystem = new FiatShamirProofSystem(invalidSigmaProtocol);
            FiatShamirProof invalidProof = invalidFiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
            invalidFiatShamirProofSystem.checkProof(testSuite.commonInput, invalidProof);
        });
    }

    @Test
    void testOr() {
        SpendDeductTree firstValidLeaf = new TokenPointsLeaf("RangeProofValid1", lowerLimits, upperLimits, true);
        SpendDeductTree secondValidLeaf = new TokenPointsLeaf("RangeProofValid2", lowerLimits, upperLimits, true);
        SpendDeductTree firstInvalidLeaf = new TokenPointsLeaf("RangeProofInvalid1", lowerLimits, invalidUpperLimits, false);
        SpendDeductTree secondInvalidLeaf = new TokenPointsLeaf("RangeProofInvalid2", lowerLimits, invalidUpperLimits, false);

        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(new SpendDeductOrNode(firstValidLeaf, secondValidLeaf), pp, promotion, providerKey.getPk());
        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));

        sigmaProtocol = new SpendDeductBooleanZkp(new SpendDeductOrNode(firstValidLeaf, secondInvalidLeaf), pp, promotion, providerKey.getPk());
        fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));

        sigmaProtocol = new SpendDeductBooleanZkp(new SpendDeductOrNode(firstInvalidLeaf, secondValidLeaf), pp, promotion, providerKey.getPk());
        fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));

        Assertions.assertThrows(RuntimeException.class, () -> {
            SpendDeductBooleanZkp invalidSigmaProtocol = new SpendDeductBooleanZkp(new SpendDeductOrNode(firstInvalidLeaf, secondInvalidLeaf), pp, promotion, providerKey.getPk());
            FiatShamirProofSystem invalidFiatShamirProofSystem = new FiatShamirProofSystem(invalidSigmaProtocol);
            FiatShamirProof invalidProof = invalidFiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
            invalidFiatShamirProofSystem.checkProof(testSuite.commonInput, invalidProof);
        });
    }

}