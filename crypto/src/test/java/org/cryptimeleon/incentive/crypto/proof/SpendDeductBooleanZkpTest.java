package org.cryptimeleon.incentive.crypto.proof;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.protocols.arguments.sigma.SigmaProtocol;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.MetadataZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendDeductZkpWitnessInput;
import org.cryptimeleon.incentive.crypto.proof.spend.TokenPointsRangeProof;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.*;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
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

    private class TestSpendDeductLeafNode extends SpendDeductLeafNode {

        public TestSpendDeductLeafNode(BigInteger[] lowerLimits, BigInteger[] upperLimits, Boolean isTrue, String name) {
            this.lowerLimits = lowerLimits;
            this.upperLimits = upperLimits;
            this.isTrue = isTrue;
            this.name = name;
        }

        private final BigInteger[] lowerLimits;
        private final BigInteger[] upperLimits;
        private final Boolean isTrue;
        private final String name;

        @Override
        public SigmaProtocol getProtocol(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
            return new TokenPointsRangeProof(pp, this.lowerLimits, this.upperLimits, providerPublicKey, promotionParameters);
        }

        @Override
        public boolean isTrue() {
            return this.isTrue;
        }

        @Override
        public String getLeafName() {
            return this.name;
        }
    }

    private static class TestClassicSpendDeductLeafNode extends SpendDeductLeafNode {

        @Override
        public SigmaProtocol getProtocol(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
            return new MetadataZkp(pp, providerPublicKey, promotionParameters);
        }

        @Override
        public boolean isTrue() {
            return true;
        }

        @Override
        public String getLeafName() {
            return "MainZKP";
        }
    }

    @BeforeEach
    void setup() {
        pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        providerKey = Setup.providerKeyGen(pp);
        userKey = Setup.userKeyGen(pp);
        incentiveSystem = new IncentiveSystem(pp);
        promotion = incentiveSystem.generatePromotionParameters(4);
        token = Helper.generateToken(pp, userKey, providerKey, promotion, Vector.fromStreamPlain(Arrays.stream(points)));
        zn = pp.getBg().getZn();
    }

    @Test
    void testClassicLeaf() {
        var testSuite = generateTestSuite();

        SpendDeductTree proofTree = new TestClassicSpendDeductLeafNode();
        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(proofTree, pp, promotion, providerKey.getPk());

        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));
    }

    @Test
    void testRangeProofLeaf() {
        var testSuite = generateTestSuite();

        SpendDeductTree proofTree = new TestSpendDeductLeafNode(lowerLimits, upperLimits, true, "range-proof");
        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(proofTree, pp, promotion, providerKey.getPk());

        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
        assertTrue(fiatShamirProofSystem.checkProof(testSuite.commonInput, proof));
    }

    @Test
    void testFalseRangeProofLeaf() {
        var testSuite = generateTestSuite();

        SpendDeductTree proofTree = new TestSpendDeductLeafNode(lowerLimits, invalidUpperLimits, false, "range-proof");
        SpendDeductBooleanZkp sigmaProtocol = new SpendDeductBooleanZkp(proofTree, pp, promotion, providerKey.getPk());

        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(sigmaProtocol);
        Assertions.assertThrows(RuntimeException.class, () -> {
            FiatShamirProof proof = fiatShamirProofSystem.createProof(testSuite.commonInput, testSuite.witness);
            fiatShamirProofSystem.checkProof(testSuite.commonInput, proof);
        });
    }

    @Test
    void testAnd() {
        var testSuite = generateTestSuite();

        SpendDeductTree firstValidLeaf = new TestSpendDeductLeafNode(lowerLimits, upperLimits, true, "range-proof-valid-1");
        SpendDeductTree secondValidLeaf = new TestSpendDeductLeafNode(lowerLimits, upperLimits, true, "range-proof-valid-2");
        SpendDeductTree invalidLeaf = new TestSpendDeductLeafNode(lowerLimits, invalidUpperLimits, false, "range-proof-invalid");

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
        var testSuite = generateTestSuite();

        SpendDeductTree firstValidLeaf = new TestSpendDeductLeafNode(lowerLimits, upperLimits, true, "range-proof-valid");
        SpendDeductTree secondValidLeaf = new TestSpendDeductLeafNode(lowerLimits, upperLimits, true, "range-proof-valid-2");
        SpendDeductTree firstInvalidLeaf = new TestSpendDeductLeafNode(lowerLimits, invalidUpperLimits, false, "range-proof-invalid-1");
        SpendDeductTree secondInvalidLeaf = new TestSpendDeductLeafNode(lowerLimits, invalidUpperLimits, false, "range-proof-invalid-2");

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

    private SpendZkpTestSuite generateTestSuite() {
        var zp = pp.getBg().getZn();
        var usk = userKey.getSk().getUsk();
        var esk = token.getEncryptionSecretKey();
        var dsid = pp.getW().pow(esk);
        var vectorH = providerKey.getPk().getH(this.pp, promotion);
        var vectorR = zp.getUniformlyRandomElements(pp.getNumEskDigits());
        var newPointsVector = RingElementVector.fromStream(Arrays.stream(newPoints).map(e -> pp.getBg().getZn().createZnElement(e)));
        var tid = zn.getUniformlyRandomElement();


        /* Compute pseudorandom values */
        // As in credit-earn, we use the PRF to make the algorithm deterministic
        var prfZnElements = pp.getPrfToZn().hashThenPrfToZnVector(userKey.getSk().getPrfKey(), token, 6, "SpendDeduct");
        Zn.ZnElement eskUsrS = (Zn.ZnElement) prfZnElements.get(0);
        Zn.ZnElement dsrnd0S = (Zn.ZnElement) prfZnElements.get(1);
        Zn.ZnElement dsrnd1S = (Zn.ZnElement) prfZnElements.get(2);
        Zn.ZnElement zS = (Zn.ZnElement) prfZnElements.get(3);
        Zn.ZnElement tS = (Zn.ZnElement) prfZnElements.get(4);
        Zn.ZnElement uS = (Zn.ZnElement) prfZnElements.get(5);

        // Prepare a new commitment (cPre0, cPre1) based on the pseudorandom values
        var exponents = new RingElementVector(tS, usk, eskUsrS, dsrnd0S, dsrnd1S, zS).concatenate(newPointsVector);
        var cPre0 = vectorH.innerProduct(exponents).pow(uS).compute();
        var cPre1 = pp.getG1Generator().pow(uS).compute();

        /* Enable double-spending-protection by forcing usk and esk becoming public in that case
           If token is used twice in two different transactions, the provider observes (c0,c1), (c0',c1') with gamma!=gamma'
           Hence, the provider can easily retrieve usk and esk (using the Schnorr-trick, computing (c0-c0')/(gamma-gamma') for usk, analogously for esk). */
        var gamma = Util.hashGamma(zp, dsid, tid, cPre0, cPre1);
        var c0 = usk.mul(gamma).add(token.getDoubleSpendRandomness0());
        var c1 = esk.mul(gamma).add(token.getDoubleSpendRandomness1());

        /* Compute El-Gamal encryption of esk^*_usr using under secret key esk
           This allows the provider to decrypt usk^*_usr in case of double spending with the leaked esk.
           By additionally storing esk^*_prov, the provider can retrieve esk^* and thus iteratively decrypt the new esks. */

        // Decompose the encryption-secret-key to base eskDecBase and map the digits to Zn
        var eskUsrSDecBigInt = IntegerRing.decomposeIntoDigits(eskUsrS.asInteger(), pp.getEskDecBase().asInteger(), pp.getNumEskDigits());
        var eskUsrSDec = RingElementVector.generate(i -> zp.valueOf(eskUsrSDecBigInt[i]), eskUsrSDecBigInt.length);

        // Encrypt digits using El-Gamal and the randomness r
        var cTrace0 = pp.getW().pow(vectorR).compute();
        var cTrace1 = cTrace0.pow(esk).op(pp.getW().pow(eskUsrSDec)).compute();

        /* Build noninteractive (Fiat-Shamir transformed) ZKP to ensure that the user follows the rules of the protocol */
        var witness = new SpendDeductZkpWitnessInput(usk, token.getZ(), zS, token.getT(), tS, uS, esk, eskUsrS, token.getDoubleSpendRandomness0(), dsrnd0S, token.getDoubleSpendRandomness1(), dsrnd1S, eskUsrSDec, vectorR, token.getPoints(), newPointsVector);
        var commonInput = new SpendDeductZkpCommonInput(gamma, c0, c1, dsid, cPre0, cPre1, token.getCommitment0(), cTrace0, cTrace1);
        return new SpendZkpTestSuite(witness, commonInput);
    }

    private class SpendZkpTestSuite {
        SpendDeductZkpCommonInput commonInput;
        SpendDeductZkpWitnessInput witness;

        public SpendZkpTestSuite(SpendDeductZkpWitnessInput witness, SpendDeductZkpCommonInput commonInput) {
            this.witness = witness;
            this.commonInput = commonInput;
        }
    }
}