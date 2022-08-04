package org.cryptimeleon.incentive.crypto.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Class that creates some random mathematical objects. Used to shorten tests and sometimes system code.
 */
public class Helper {
    /**
     * Generates a sound empty (i.e. no points) user token as output by a sound execution of the Issue-Join protocol.
     */
    public static Token generateToken(IncentivePublicParameters pp,
                                      UserKeyPair userKeyPair,
                                      ProviderKeyPair providerKeyPair,
                                      PromotionParameters promotionParameters) {
        return generateToken(pp,
                userKeyPair,
                providerKeyPair,
                promotionParameters,
                Vector.iterate(BigInteger.valueOf(0), v -> v, promotionParameters.getPointsVectorSize()));
    }

    /**
     * Generates a valid user token, as output by a sound execution of the Issue-Join protocol followed by an execution of Credit-Earn with the passed earn vector.
     */
    public static Token generateToken(IncentivePublicParameters pp,
                                      UserKeyPair userKeyPair,
                                      ProviderKeyPair providerKeyPair,
                                      PromotionParameters promotionParameters,
                                      Vector<BigInteger> points) {
        var vectorH = providerKeyPair.getPk().getH(pp, promotionParameters);
        var zp = pp.getBg().getZn();
        // Manually create a token since issue-join is not yet implemented
        var encryptionSecretKey = zp.getUniformlyRandomNonzeroElement();
        var dsrd1 = zp.getUniformlyRandomElement();
        var dsrd2 = zp.getUniformlyRandomElement();
        var z = zp.getUniformlyRandomElement();
        var t = zp.getUniformlyRandomElement();
        var pointsVector = RingElementVector.fromStream(points.stream().map(e -> pp.getBg().getZn().createZnElement(e)));
        var exponents = RingElementVector.of(
                t, userKeyPair.getSk().getUsk(), encryptionSecretKey, dsrd1, dsrd2, z
        );
        exponents = exponents.concatenate(pointsVector);
        var c1 = vectorH.innerProduct(exponents).compute();
        var c2 = pp.getG1Generator();

        return new Token(
                c1,
                c2,
                encryptionSecretKey,
                dsrd1,
                dsrd2,
                z,
                t,
                promotionParameters.getPromotionId(),
                pointsVector,
                (SPSEQSignature) pp.getSpsEq().sign(
                        providerKeyPair.getSk().getSkSpsEq(),
                        c1,
                        c2,
                        c2.pow(promotionParameters.getPromotionId())
                )
        );
    }

    /**
     * Generates a sound transaction that spends the passed token.
     *
     * @return spend-deduct output, consisting of this transaction and the result token
     */
    public static SpendDeductOutput generateSoundTransaction(IncentiveSystem incSys,
                                                             PromotionParameters promP,
                                                             Token token,
                                                             ProviderKeyPair pkp,
                                                             UserKeyPair ukp,
                                                             Vector<BigInteger> newPoints,
                                                             Zn.ZnElement tid,
                                                             SpendDeductTree spendDeductTree
    ) {
        var spendRequest = incSys.generateSpendRequest(promP, token, pkp.getPk(), newPoints, ukp, tid, spendDeductTree);

        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var deductOutput = incSys.generateSpendRequestResponse(promP, spendRequest, pkp, tid, spendDeductTree, tid);

        var resultToken = incSys.handleSpendRequestResponse(promP, deductOutput.getSpendResponse(), spendRequest, token, newPoints, pkp.getPk(), ukp);

        var occuredTransaction = new Transaction(
                true,
                tid,
                "teddy bear",
                promP.getPromotionId(),
                deductOutput.getDstag()
        );

        return new SpendDeductOutput(resultToken, occuredTransaction);
    }

    /**
     * Creates and returns a transaction with spend amount 1 and random other fields.
     *
     * @param valid whether the generated transaction shall be valid or not
     */
    public static Transaction generateRandomTransaction(IncentivePublicParameters pp, boolean valid) {
        Zn usedZn = pp.getBg().getZn();
        Group usedG1 = pp.getBg().getG1();

        return new Transaction(
                valid,
                usedZn.getUniformlyRandomElement(),
                "teddy bear",
                BigInteger.ONE, // Promotion ID (same promotion)
                new DoubleSpendingTag(
                        usedZn.getUniformlyRandomElement(),
                        usedZn.getUniformlyRandomElement(),
                        usedZn.getUniformlyRandomElement(),
                        usedZn.getUniformlyRandomElement(),
                        usedG1.getUniformlyRandomElements(pp.getNumEskDigits()),
                        usedG1.getUniformlyRandomElements(pp.getNumEskDigits())
                )
        );
    }

    /**
     * Creates random user info.
     *
     * @return UserInfo
     */
    public static UserInfo generateRandomUserInfo(IncentivePublicParameters pp) {
        Zn usedZn = pp.getBg().getZn();
        Group usedG1 = pp.getBg().getG1();

        return new UserInfo(
                new UserPublicKey(usedG1.getUniformlyRandomElement()),
                usedZn.getUniformlyRandomElement(),
                usedZn.getUniformlyRandomElement()
        );
    }

    /**
     * Helper method to shorten code, returns a serialized representation of the passed representable.
     */
    public static String computeSerializedRepresentation(Representable r) {
        JSONConverter jsonConverter = new JSONConverter();
        return jsonConverter.serialize(
                r.getRepresentation()
        );
    }

    /**
     * Helper that generates a simple SpendDeductTree which represents a ZKP that checks whether there are enough points
     * to spend and then spends the points.
     *
     * @param promotionParameters the promotion parameters
     * @param subtractPoints      the points that shall be subtracted from the user's token
     * @return a spendDeductTree for this statement
     */
    public static SpendDeductTree generateSimpleTestSpendDeductZkp(PromotionParameters promotionParameters,
                                                                   Vector<BigInteger> subtractPoints) {

        Vector<BigInteger> ignore = Util.getNullBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> ones = Util.getOneBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> negatedSubtractPoints = Vector.fromStreamPlain(subtractPoints.stream().map(BigInteger::negate));

        return generateTestZkp(
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
     * {@literal ^ lowerLimits <= points <= upperLimits}
     * {@literal ^ newLowerLimits <= newPoints <= newUpperLimits}
     * {@literal ^ for all i: newPoints_i = a_i * oldPoints_i + b_i}
     *
     * @param lowerLimits    vector of lower limits for the old points, null means not checked
     * @param upperLimits    vector of upper limits for the old points, null means not checked
     * @param newLowerLimits vector of lower limits for the new points, null means not checked
     * @param newUpperLimits vector of upper limits for the new points, null means not checked
     * @param aVector        vector of factors a_i for the linear relation proof. Ignore if null
     * @param bVector        vector of summands b_i for the linear relation proof. Ignore if null
     * @return spendDeductTree for the upper statement
     */
    public static SpendDeductTree generateTestZkp(Vector<BigInteger> lowerLimits,
                                                  Vector<BigInteger> upperLimits,
                                                  Vector<BigInteger> newLowerLimits,
                                                  Vector<BigInteger> newUpperLimits,
                                                  Vector<BigInteger> aVector,
                                                  Vector<BigInteger> bVector) {

        SpendDeductTree conditionTree = new TokenPointsLeaf("RangeProof", lowerLimits, upperLimits);
        SpendDeductTree updateTree = new TokenUpdateLeaf("UpdateProof", newLowerLimits, newUpperLimits, aVector, bVector);
        return new SpendDeductAndNode(updateTree, conditionTree);
    }
}
