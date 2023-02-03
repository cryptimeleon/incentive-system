package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.ZnChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearExponentStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendThenDelegateFragment;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.math.expressions.exponent.ExponentExpr;
import org.cryptimeleon.math.structures.cartesian.ExponentExpressionVector;
import org.cryptimeleon.math.structures.cartesian.GroupElementExpressionVector;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * ZKP to proof knowledge of correct metadata witnesses for a token.
 * Contains all but the points range proof of the paper's spend-deduct ZKP.
 */
class MetadataZkp extends DelegateProtocol {

    private final IncentivePublicParameters pp;
    private final Zn zn;
    private final ProviderPublicKey providerPublicKey;
    private final PromotionParameters promotionParameters;

    public MetadataZkp(IncentivePublicParameters incentivePublicParameters, ProviderPublicKey providerPublicKey, PromotionParameters promotionParameters) {
        this.providerPublicKey = providerPublicKey;
        this.pp = incentivePublicParameters;
        this.zn = incentivePublicParameters.getBg().getZn();
        this.promotionParameters = promotionParameters;
    }


    @Override
    protected SendThenDelegateFragment.SubprotocolSpec provideSubprotocolSpec(CommonInput pCommonInput, SendThenDelegateFragment.SubprotocolSpecBuilder builder) {
        var commonInput = (SpendDeductZkpCommonInput) pCommonInput;
        var H = new GroupElementExpressionVector(providerPublicKey.getH(pp, promotionParameters).map(GroupElement::expr));

        // Variables to use
        var uskVar = builder.addZnVariable("usk", zn);
        var dsidUserStarVar = builder.addZnVariable("dsidUserStar", zn);
        var dsrndVar = builder.addZnVariable("dsrnd", zn);
        var dsrndStarVar = builder.addZnVariable("dsrndStar", zn);
        var zVar = builder.addZnVariable("z", zn);
        var tVar = builder.addZnVariable("t", zn);
        var zStarVar = builder.addZnVariable("zStar", zn);
        var tStarVar = builder.addZnVariable("tStar", zn);
        var uStarInverseVar = builder.addZnVariable("uStarInverse", zn);
        var pointsVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("points_" + i, zn), promotionParameters.getPointsVectorSize());
        var newPointsVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("newPoints_" + i, zn), promotionParameters.getPointsVectorSize());

        // c0=usk*gamma+dsrnd
        builder.addSubprotocol("c=usk*gamma+dsrnd", new LinearExponentStatementFragment(uskVar.mul(commonInput.gamma).add(dsrndVar).isEqualTo(commonInput.c), zn));

        // C=(H.pow(usk, dsid, dsrnd, v, z, t),g_1) split into two subprotocols
        var commitmentC0Statement = H.innerProduct(ExponentExpressionVector.of(tVar, uskVar, commonInput.dsid, dsrndVar, zVar).concatenate(pointsVector.map(e -> e))).isEqualTo(commonInput.commitmentC0);
        builder.addSubprotocol("C0", new LinearStatementFragment(commitmentC0Statement));
        // C1=g is not sent and verified since no witness is involved.

        // C=(H.pow(t^*, usk, dsid_user^*, dsrnd^*, z^*, V-K), g_1^(u^*)) split into two subprotocols
        var exponents = new Vector<ExponentExpr>(tStarVar, uskVar, dsidUserStarVar, dsrndStarVar, zStarVar).concatenate(newPointsVector);
        var cPre0Statement = H.innerProduct(exponents).isEqualTo(commonInput.c0Pre.pow(uStarInverseVar));
        builder.addSubprotocol("C0Pre", new LinearStatementFragment(cPre0Statement));
        builder.addSubprotocol("C1Pre", new LinearStatementFragment(commonInput.c1Pre.pow(uStarInverseVar).isEqualTo(pp.getG1Generator()))); // Use the inverse of uStar to linearize this expression

        return builder.build();
    }

    @Override
    protected SendThenDelegateFragment.ProverSpec provideProverSpecWithNoSendFirst(CommonInput pCommonInput, SecretInput pSecretInput, SendThenDelegateFragment.ProverSpecBuilder builder) {
        var secretInput = (SpendDeductZkpWitnessInput) pSecretInput;

        // Add variables to witness
        builder.putWitnessValue("usk", secretInput.usk);
        builder.putWitnessValue("dsidUserStar", secretInput.dsidUserStar);
        builder.putWitnessValue("dsrnd", secretInput.dsrnd);
        builder.putWitnessValue("dsrndStar", secretInput.dsrndStar);
        builder.putWitnessValue("z", secretInput.z);
        builder.putWitnessValue("t", secretInput.t);
        builder.putWitnessValue("zStar", secretInput.zStar);
        builder.putWitnessValue("tStar", secretInput.tStar);
        builder.putWitnessValue("uStarInverse", secretInput.uStar.inv());

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
