package org.cryptimeleon.incentive.crypto.proof.spend;

import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpWitnessInput;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class SpendHelper {

    /**
     * Helper that generates a simple SpendDeduct Zkp that checks whether there are enough points to spend and then
     * spends the points.
     *
     * @param promotionParameters the promotion parameters
     * @param subtractPoints      the points that shall be subtracted from the user's token
     * @return a zero knowledge proof for this statement
     */
    public static SpendDeductTree generateSimpleTestSpendDeductTree(PromotionParameters promotionParameters,
                                                                    Vector<BigInteger> subtractPoints) {

        Vector<BigInteger> ignore = Util.getNullBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> ones = Util.getOneBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> negatedSubtractPoints = Vector.fromStreamPlain(subtractPoints.stream().map(BigInteger::negate));

        return generateTestTree(
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
     * @param lowerLimits         vector of lower limits for the old points, null means not checked
     * @param upperLimits         vector of upper limits for the old points, null means not checked
     * @param newLowerLimits      vector of lower limits for the new points, null means not checked
     * @param newUpperLimits      vector of upper limits for the new points, null means not checked
     * @param aVector             vector of factors a_i for the linear relation proof. Ignore if null
     * @param bVector             vector of summands b_i for the linear relation proof. Ignore if null
     * @return SpendDeductZkp for the upper statement
     */
    public static SpendDeductTree generateTestTree(Vector<BigInteger> lowerLimits,
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
        var dsid = token.getDoubleSpendingId();
        var vectorH = providerKey.getPk().getH(pp, promotion);
        var newPointsVector = RingElementVector.fromStream(newPoints.stream().map(e -> pp.getBg().getZn().createZnElement(e)));
        var tid = zn.getUniformlyRandomElement();


        /* Compute pseudorandom values */
        // As in credit-earn, we use the PRF to make the algorithm deterministic
        var prfZnElements = pp.getPrfToZn().hashThenPrfToZnVector(userKey.getSk().getPrfKey(), token, 5, "SpendDeduct");
        Zn.ZnElement dsidUserS = (Zn.ZnElement) prfZnElements.get(0);
        Zn.ZnElement dsrndS = (Zn.ZnElement) prfZnElements.get(1);
        Zn.ZnElement zS = (Zn.ZnElement) prfZnElements.get(2);
        Zn.ZnElement tS = (Zn.ZnElement) prfZnElements.get(3);
        Zn.ZnElement uS = (Zn.ZnElement) prfZnElements.get(4);

        // Prepare a new commitment (cPre0, cPre1) based on the pseudorandom values
        var exponents = new RingElementVector(tS, usk, dsidUserS, dsrndS, zS).concatenate(newPointsVector);
        var cPre0 = vectorH.innerProduct(exponents).pow(uS).compute();
        var cPre1 = pp.getG1Generator().pow(uS).compute();

        /* Enable double-spending-protection by forcing usk becoming public in that case
           If token is used twice in two different transactions, the provider observes c, 0 with gamma!=gamma'
           Hence, the provider can easily retrieve usk and esk (using the Schnorr-trick, computing (c-c')/(gamma-gamma') for usk). */
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var gamma = Util.hashGammaOld(zp, dsid, tid, cPre0, cPre1, tid);
        var c = usk.mul(gamma).add(token.getDoubleSpendRandomness());

        /* Build noninteractive (Fiat-Shamir transformed) ZKP to ensure that the user follows the rules of the protocol */
        var witness = new SpendDeductZkpWitnessInput(usk, token.getZ(), zS, token.getT(), tS, uS, dsidUserS, token.getDoubleSpendRandomness(), dsrndS, token.getPoints(), newPointsVector);
        var commonInput = new SpendDeductZkpCommonInput(gamma, c, dsid, cPre0, cPre1, token.getCommitment0());
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
