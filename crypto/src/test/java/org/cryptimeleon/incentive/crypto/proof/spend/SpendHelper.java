package org.cryptimeleon.incentive.crypto.proof.spend;

import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpWitnessInput;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class SpendHelper {

    /**
     * Helper that generates a simple SpendDeduct Zkp that checks whether there are enough points to spend and then
     * spends the points.
     *
     * @param pp                  the public parameters
     * @param promotionParameters the promotion parameters
     * @param providerPublicKey   the provider public key to use
     * @param subtractPoints      the points that shall be subtracted from the user's token
     * @return a zero knowledge proof for this statement
     */
    public static SpendDeductTree generateSimpleTestSpendDeductTree(IncentivePublicParameters pp,
                                                                    PromotionParameters promotionParameters,
                                                                    ProviderPublicKey providerPublicKey,
                                                                    Vector<BigInteger> subtractPoints) {

        Vector<BigInteger> ignore = Util.getNullBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> ones = Util.getOneBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> negatedSubtractPoints = Vector.fromStreamPlain(subtractPoints.stream().map(BigInteger::negate));

        return generateTestTree(
                pp,
                promotionParameters,
                providerPublicKey,
                subtractPoints,
                ignore,
                ignore,
                ignore,
                ones,
                negatedSubtractPoints);
    }

    /**
     * Helper that generates a ZKP of the following form over the old and new point vectors:
     * <p>
     * Metadata correct
     * ^ lowerLimits <= points <= upperLimits
     * ^ newLowerLimits <= newPoints <= newUpperLimits
     * ^ for all i: newPoints_i = a_i * oldPoints_i + b_i
     *
     * @param pp                  the public parameters used for this
     * @param promotionParameters the promotion parameters used. Point vector size must match the BigInteger[] sizes!
     * @param providerPublicKey   the providerPublicKey
     * @param lowerLimits         vector of lower limits for the old points, null means not checked
     * @param upperLimits         vector of upper limits for the old points, null means not checked
     * @param newLowerLimits      vector of lower limits for the new points, null means not checked
     * @param newUpperLimits      vector of upper limits for the new points, null means not checked
     * @param aVector             vector of factors a_i for the linear relation proof. Ignore if null
     * @param bVector             vector of summands b_i for the linear relation proof. Ignore if null
     * @return SpendDeductZkp for the upper statement
     */
    public static SpendDeductTree generateTestTree(IncentivePublicParameters pp,
                                                   PromotionParameters promotionParameters,
                                                   ProviderPublicKey providerPublicKey,
                                                   Vector<BigInteger> lowerLimits,
                                                   Vector<BigInteger> upperLimits,
                                                   Vector<BigInteger> newLowerLimits,
                                                   Vector<BigInteger> newUpperLimits,
                                                   Vector<BigInteger> aVector,
                                                   Vector<BigInteger> bVector) {

        SpendDeductTree conditionTree = new TokenPointsLeaf("RangeProof", lowerLimits, upperLimits);
        SpendDeductTree updateTree = new TokenUpdateLeaf("UpdateProof", newLowerLimits, newUpperLimits, aVector, bVector);
        return new SpendDeductAndNode(updateTree, conditionTree);
    }

    public static SpendZkpTestSuite generateTestSuite(Vector<BigInteger> newPoints, IncentivePublicParameters pp, PromotionParameters promotion, ProviderKeyPair providerKey, Token token, UserKeyPair userKey, Zn zn) {
        var zp = pp.getBg().getZn();
        var usk = userKey.getSk().getUsk();
        var dsid = pp.getW().pow(0);
        var vectorH = providerKey.getPk().getH(pp, promotion);
        var vectorR = zp.getUniformlyRandomElements(pp.getNumEskDigits());
        var newPointsVector = RingElementVector.fromStream(newPoints.stream().map(e -> pp.getBg().getZn().createZnElement(e)));
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
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var gamma = Util.hashGammaOld(zp, dsid, tid, cPre0, cPre1, tid);
        var c0 = usk.mul(gamma).add(token.getDoubleSpendRandomness0());
        Zn.ZnElement c1 = null;

        /* Compute El-Gamal encryption of esk^*_usr using under secret key esk
           This allows the provider to decrypt usk^*_usr in case of double spending with the leaked esk.
           By additionally storing esk^*_prov, the provider can retrieve esk^* and thus iteratively decrypt the new esks. */

        // Decompose the encryption-secret-key to base eskDecBase and map the digits to Zn
        var eskUsrSDecBigInt = IntegerRing.decomposeIntoDigits(eskUsrS.asInteger(), pp.getEskDecBase().asInteger(), pp.getNumEskDigits());
        var eskUsrSDec = RingElementVector.generate(i -> zp.valueOf(eskUsrSDecBigInt[i]), eskUsrSDecBigInt.length);

        // Encrypt digits using El-Gamal and the randomness r
        var cTrace0 = pp.getW().pow(vectorR).compute();
        GroupElementVector cTrace1 = null;

        /* Build noninteractive (Fiat-Shamir transformed) ZKP to ensure that the user follows the rules of the protocol */
        var witness = new SpendDeductZkpWitnessInput(usk, token.getZ(), zS, token.getT(), tS, uS, null, eskUsrS, token.getDoubleSpendRandomness0(), dsrnd0S, token.getDoubleSpendRandomness1(), dsrnd1S, eskUsrSDec, vectorR, token.getPoints(), newPointsVector);
        var commonInput = new SpendDeductZkpCommonInput(gamma, c0, c1, dsid, cPre0, cPre1, token.getCommitment0(), cTrace0, cTrace1);
        return new SpendZkpTestSuite(witness, commonInput);
    }

    public static class SpendZkpTestSuite {
        public final SpendDeductZkpCommonInput commonInput;
        public final SpendDeductZkpWitnessInput witness;

        public SpendZkpTestSuite(SpendDeductZkpWitnessInput witness, SpendDeductZkpCommonInput commonInput) {
            this.witness = witness;
            this.commonInput = commonInput;
        }
    }
}
