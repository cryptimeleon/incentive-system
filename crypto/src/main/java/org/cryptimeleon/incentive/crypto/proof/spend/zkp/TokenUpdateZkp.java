package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.ZnChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearExponentStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendThenDelegateFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.setmembership.SmallerThanPowerFragment;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.math.expressions.exponent.ExponentConstantExpr;
import org.cryptimeleon.math.structures.cartesian.ExponentExpressionVector;
import org.cryptimeleon.math.structures.cartesian.GroupElementExpressionVector;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * A zero knowledge proof for defining update relations between the old and new token.
 */
class TokenUpdateZkp extends DelegateProtocol {

    // Public parameters
    final IncentivePublicParameters pp;
    // Proof that lowerLimits[i] <= newPoints[i] <= upperLimits[i] for all i
    final Vector<BigInteger> lowerLimits; // null means no limit
    final Vector<BigInteger> upperLimits; // null means no limit
    // Prove linear relation between new points and old points
    // newPoints[i] = a[i]* oldPoints[i] + bVector[i]
    final Vector<BigInteger> aVector; // null means no statement
    final Vector<BigInteger> bVector; // null means no statement
    final ProviderPublicKey providerPublicKey;
    final PromotionParameters promotionParameters;

    public TokenUpdateZkp(IncentivePublicParameters pp, Vector<BigInteger> lowerLimits, Vector<BigInteger> upperLimits, Vector<BigInteger> aVector, Vector<BigInteger> bVector, ProviderPublicKey providerPublicKey, PromotionParameters promotionParameters) {
        this.pp = pp;
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
        this.aVector = aVector;
        this.bVector = bVector;
        this.providerPublicKey = providerPublicKey;
        this.promotionParameters = promotionParameters;
    }

    @Override
    protected SendThenDelegateFragment.SubprotocolSpec provideSubprotocolSpec(CommonInput pCommonInput, SendThenDelegateFragment.SubprotocolSpecBuilder builder) {
        var commonInput = (SpendDeductZkpCommonInput) pCommonInput;
        var H = new GroupElementExpressionVector(providerPublicKey.getH(pp, promotionParameters).map(GroupElement::expr));
        var zn = pp.getBg().getZn();

        // Variables to use
        var eskVar = builder.addZnVariable("esk", zn);
        var uskVar = builder.addZnVariable("usk", zn);
        var dsrnd0Var = builder.addZnVariable("dsrnd0", zn);
        var dsrnd1Var = builder.addZnVariable("dsrnd1", zn);
        var zVar = builder.addZnVariable("z", zn);
        var tVar = builder.addZnVariable("t", zn);
        var pointsVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("points_" + i, zn), promotionParameters.getPointsVectorSize());
        var newPointsVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("newPoints_" + i, zn), promotionParameters.getPointsVectorSize());
        var dsrndStar0Var = builder.addZnVariable("dsrndStar0", zn);
        var dsrndStar1Var = builder.addZnVariable("dsrndStar1", zn);
        var zStarVar = builder.addZnVariable("zStar", zn);
        var tStarVar = builder.addZnVariable("tStar", zn);
        var uStarInverseVar = builder.addZnVariable("uStarInverse", zn);
        var eskDecVarVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("eskStarUserDec_" + i, zn), pp.getNumEskDigits());


        // C=(H.pow(t, usk, esk, dsrnd_0, dsrnd_1, points, z), g_1)
        var commitmentC0Statement = H.innerProduct(ExponentExpressionVector.of(tVar, uskVar, eskVar, dsrnd0Var, dsrnd1Var, zVar).concatenate(pointsVector.map(e -> e))).isEqualTo(commonInput.commitmentC0);
        builder.addSubprotocol("C0", new LinearStatementFragment(commitmentC0Statement));

        // C_1=H.pow(t^*, usk, \sum_i=0^k[esk^*_(usr,i) * base^i], dsrnd^*_0, dsrnd^*_1, z^*, newPointsVector)
        var powersOfEskDecBase = ExponentExpressionVector.generate(i -> pp.getEskDecBase().pow(BigInteger.valueOf(i)).asExponentExpression(), pp.getNumEskDigits()); // construct vector (eskBase^0, eskBase^1, ...)
        var exponents = new Vector<>(tStarVar, uskVar, eskDecVarVector.innerProduct(powersOfEskDecBase), dsrndStar0Var, dsrndStar1Var, zStarVar).concatenate(newPointsVector);
        builder.addSubprotocol("C0Pre", new LinearStatementFragment(H.innerProduct(exponents).isEqualTo(commonInput.c0Pre.pow(uStarInverseVar))));

        // Add range proofs
        for (int i = 0; i < promotionParameters.getPointsVectorSize(); i++) {
            if (lowerLimits.get(i) != null) {
                builder.addSubprotocol(
                        "newPointsVector[" + i + "]>=lowerLimits[" + i + "]",
                        new SmallerThanPowerFragment(
                                newPointsVector.get(i).sub(lowerLimits.get(i).intValue()),
                                pp.getEskDecBase().asInteger().intValue(),
                                pp.getMaxPointBasePower(),
                                pp.getEskBaseSetMembershipPublicParameters()
                        )
                );
            }
            if (upperLimits.get(i) != null) {
                builder.addSubprotocol(
                        "newPointsVector[" + i + "]<=upperLimits[" + i + "]",
                        new SmallerThanPowerFragment(
                                (new ExponentConstantExpr(upperLimits.get(i))).sub(newPointsVector.get(i)),
                                pp.getEskDecBase().asInteger().intValue(),
                                pp.getMaxPointBasePower(),
                                pp.getEskBaseSetMembershipPublicParameters()
                        )
                );
            }
        }

        // Add affine linear statements
        for (int i = 0; i < promotionParameters.getPointsVectorSize(); i++) {
            if (aVector.get(i) != null && bVector.get(i) != null) {
                builder.addSubprotocol(
                        "newPointsVector[" + i + "]=aVector[" + i + "]*pointsVector[" + i + "]+bVector[" + i + "]",
                        new LinearExponentStatementFragment(
                                pointsVector.get(i).mul(aVector.get(i)).add(bVector.get(i)).isEqualTo(newPointsVector.get(i)), zn
                        )
                );

            }
        }

        return builder.build();
    }

    @Override
    protected SendThenDelegateFragment.ProverSpec provideProverSpecWithNoSendFirst(CommonInput commonInput, SecretInput pSecretInput, SendThenDelegateFragment.ProverSpecBuilder builder) {
        var secretInput = (SpendDeductZkpWitnessInput) pSecretInput;

        // Add variables to witness
        builder.putWitnessValue("esk", secretInput.esk);
        builder.putWitnessValue("usk", secretInput.usk);
        builder.putWitnessValue("dsrnd0", secretInput.dsrnd0);
        builder.putWitnessValue("dsrnd1", secretInput.dsrnd1);
        builder.putWitnessValue("dsrndStar0", secretInput.dsrndStar0);
        builder.putWitnessValue("dsrndStar1", secretInput.dsrndStar1);
        builder.putWitnessValue("z", secretInput.z);
        builder.putWitnessValue("t", secretInput.t);
        builder.putWitnessValue("zStar", secretInput.zStar);
        builder.putWitnessValue("tStar", secretInput.tStar);
        builder.putWitnessValue("uStarInverse", secretInput.uStar.inv());

        for (int i = 0; i < pp.getNumEskDigits(); i++) {
            builder.putWitnessValue("eskStarUserDec_" + i, (Zn.ZnElement) secretInput.eskStarUserDec.get(i));
        }

        for (int i = 0; i < promotionParameters.getPointsVectorSize(); i++) {
            builder.putWitnessValue("points_" + i, (Zn.ZnElement) secretInput.pointsVector.get(i));
            builder.putWitnessValue("newPoints_" + i, (Zn.ZnElement) secretInput.newPointsVector.get(i));
        }

        return builder.build();
    }

    @Override
    public ZnChallengeSpace getChallengeSpace(CommonInput commonInput) {
        return new ZnChallengeSpace(pp.getBg().getZn());
    }
}
