package org.cryptimeleon.incentive.crypto.proof.spend;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductOrNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpendDeductBooleanZkpTest {

    Vector<BigInteger> points = Vector.of(
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3),
            BigInteger.valueOf(3)
    );
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
    Vector<BigInteger> invalidUpperLimits = Vector.of(
            BigInteger.valueOf(2),
            null,
            null,
            null
    );
    Vector<BigInteger> newPoints = Vector.of(
            BigInteger.valueOf(2),
            BigInteger.valueOf(1),
            BigInteger.valueOf(0),
            BigInteger.valueOf(3)
    );

    IncentivePublicParameters pp;
    ProviderKeyPair providerKey;
    UserKeyPair userKey;
    PromotionParameters promotion;
    Token token;
    Zn zn;
    SpendHelper.SpendZkpTestSuite testSuite;
    RingElementVector pointsREV;
    RingElementVector newPointsREV;

    @BeforeEach
    void setup() {
        pp = TestSuite.pp;
        providerKey = TestSuite.providerKeyPair;
        userKey = TestSuite.userKeyPair;
        promotion = IncentiveSystem.generatePromotionParameters(4);
        token = TestSuite.generateToken(promotion, points);
        zn = pp.getBg().getZn();
        testSuite = SpendHelper.generateTestSuite(newPoints, pp, promotion, providerKey, token, userKey, zn);
        pointsREV = new RingElementVector(Vector.fromStreamPlain(points.stream().map(this.zn::getElement)));
        newPointsREV = new RingElementVector(Vector.fromStreamPlain(newPoints.stream().map(this.zn::getElement)));
    }


    @Test
    void testRangeProofLeaf() {
        var proofTree = new TokenPointsLeaf("RangeProof", lowerLimits, upperLimits);

        assertTrue(proofTree.isValidForPoints(points, newPoints));

        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(proofTree, pp, promotion, providerKey.getPk());

        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));
    }

    @Test
    void testFalseRangeProofLeaf() {
        SpendDeductTree proofTree = new TokenPointsLeaf("RangeProof", lowerLimits, invalidUpperLimits);
        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(proofTree, pp, promotion, providerKey.getPk());

        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        Assertions.assertThrows(RuntimeException.class, () -> {
            FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
            fiatShamirProofSystem.checkProof(testSuite.commonInput, proof);
        });
    }

    @Test
    void testAnd() {
        SpendDeductTree firstValidLeaf = new TokenPointsLeaf("RangeProof1", lowerLimits, upperLimits);
        SpendDeductTree secondValidLeaf = new TokenPointsLeaf("RangeProof2", lowerLimits, upperLimits);
        SpendDeductTree invalidLeaf = new TokenPointsLeaf("RangeProof3", lowerLimits, invalidUpperLimits);

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
        var firstValidLeaf = new TokenPointsLeaf("RangeProofValid1", lowerLimits, upperLimits);
        var secondValidLeaf = new TokenPointsLeaf("RangeProofValid2", lowerLimits, upperLimits);
        var firstInvalidLeaf = new TokenPointsLeaf("RangeProofInvalid1", lowerLimits, invalidUpperLimits);
        var secondInvalidLeaf = new TokenPointsLeaf("RangeProofInvalid2", lowerLimits, invalidUpperLimits);

        // Check isValidForPoints works correctly
        assertTrue(firstValidLeaf.isValidForPoints(points, newPoints));
        assertTrue(secondValidLeaf.isValidForPoints(points, newPoints));
        assertFalse(firstInvalidLeaf.isValidForPoints(points, newPoints));
        assertFalse(secondInvalidLeaf.isValidForPoints(points, newPoints));

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
