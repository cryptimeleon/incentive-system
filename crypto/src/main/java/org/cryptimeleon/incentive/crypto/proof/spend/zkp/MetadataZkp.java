package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.ZnChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearExponentStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendThenDelegateFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.setmembership.SetMembershipFragment;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.math.structures.cartesian.ExponentExpressionVector;
import org.cryptimeleon.math.structures.cartesian.GroupElementExpressionVector;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * The ZKP used in the spend-deduct protocol
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
        var w = pp.getW();

        // Variables to use
        var eskVar = builder.addZnVariable("esk", zn);
        var uskVar = builder.addZnVariable("usk", zn);
        var dsrnd0Var = builder.addZnVariable("dsrnd0", zn);
        var dsrnd1Var = builder.addZnVariable("dsrnd1", zn);
        var dsrndStar0Var = builder.addZnVariable("dsrndStar0", zn);
        var dsrndStar1Var = builder.addZnVariable("dsrndStar1", zn);
        var zVar = builder.addZnVariable("z", zn);
        var tVar = builder.addZnVariable("t", zn);
        var zStarVar = builder.addZnVariable("zStar", zn);
        var tStarVar = builder.addZnVariable("tStar", zn);
        var uStarInverseVar = builder.addZnVariable("uStarInverse", zn);
        var eskDecVarVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("eskStarUserDec_" + i, zn), pp.getNumEskDigits());
        var rVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("r_" + i, zn), pp.getNumEskDigits());
        var pointsVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("points_" + i, zn), promotionParameters.getPointsVectorSize());
        var newPointsVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("newPoints_" + i, zn), promotionParameters.getPointsVectorSize());

        // c0=usk*gamma+dsrnd0
        builder.addSubprotocol(
                "c0=usk*gamma+dsrnd0",
                new LinearExponentStatementFragment(uskVar.mul(commonInput.gamma).add(dsrnd0Var).isEqualTo(commonInput.c0), zn)
        );

        // c1=esk*gamma+dsrnd1
        builder.addSubprotocol(
                "c1=esk*gamma+dsrnd1",
                new LinearExponentStatementFragment(eskVar.mul(commonInput.gamma).add(dsrnd1Var).isEqualTo(commonInput.c1), zn)
        );

        // dsid=w^esk
        var dsidEskStatement = w.pow(eskVar).isEqualTo(commonInput.dsid);
        builder.addSubprotocol("dsid=w^esk", new LinearStatementFragment(dsidEskStatement));

        // C=(H.pow(usk, esk, dsrnd_0, dsrnd_1, v, z, t),g_1) split into two subprotocols
        var commitmentC0Statement = H.innerProduct(
                ExponentExpressionVector.of(tVar, uskVar, eskVar, dsrnd0Var, dsrnd1Var, zVar).concatenate(pointsVector.map(e -> e))
        ).isEqualTo(commonInput.commitmentC0);
        builder.addSubprotocol("C0", new LinearStatementFragment(commitmentC0Statement));
        // C1=g is not sent and verified since no witness is involved.

        // C=(H.pow(t^*, usk, \sum_i=0^k[esk^*_(usr,i) * base^i], dsrnd^*_0, dsrnd^*_1, z^*, V-K), g_1^(u^*)) split into two subprotocols
        // We use the sum to combine the esk^*_usr = \sum proof with the C=.. proof
        var powersOfEskDecBase = ExponentExpressionVector.generate(i -> pp.getEskDecBase().pow(BigInteger.valueOf(i)).asExponentExpression(), pp.getNumEskDigits()); // construct vector (eskBase^0, eskBase^1, ...)
        var exponents = new Vector<>(
                tStarVar,
                uskVar,
                eskDecVarVector.innerProduct(powersOfEskDecBase),
                dsrndStar0Var,
                dsrndStar1Var,
                zStarVar
        ).concatenate(newPointsVector);
        var cPre0Statement = H.innerProduct(exponents).isEqualTo(commonInput.c0Pre.pow(uStarInverseVar));
        builder.addSubprotocol("C0Pre", new LinearStatementFragment(cPre0Statement));
        builder.addSubprotocol("C1Pre", new LinearStatementFragment(commonInput.c1Pre.pow(uStarInverseVar).isEqualTo(pp.getG1Generator()))); // Use the inverse of uStar to linearize this expression

        // esk^*_(usr,i)\in[0,eskDecBase-1]
        for (int i = 0; i < pp.getNumEskDigits(); i++) {
            builder.addSubprotocol("eskDigitSetMembership" + i, new SetMembershipFragment(pp.getEskBaseSetMembershipPublicParameters(), eskDecVarVector.get(i)));
        }


        // ctrace=(w^{r_i} ,{w^{r_i}}^{esk}*w^{esk^*_{usr,i}}) for all i\in[p]
        for (int i = 0; i < pp.getNumEskDigits(); i++) {
            builder.addSubprotocol("ctrace0" + i, new LinearStatementFragment(w.pow(rVector.get(i)).isEqualTo(commonInput.ctrace0.get(i))));
            builder.addSubprotocol("ctrace1" + i, new LinearStatementFragment(w.pow(eskDecVarVector.get(i)).op(commonInput.ctrace0.get(i).pow(eskVar)).isEqualTo(commonInput.ctrace1.get(i))));
        }

        return builder.build();
    }

    @Override
    protected SendThenDelegateFragment.ProverSpec provideProverSpecWithNoSendFirst(CommonInput pCommonInput, SecretInput pSecretInput, SendThenDelegateFragment.ProverSpecBuilder builder) {
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
            builder.putWitnessValue("r_" + i, (Zn.ZnElement) secretInput.rVector.get(i));
        }

        for (int i = 0; i < promotionParameters.getPointsVectorSize(); i++) {
            builder.putWitnessValue("points_" + i, (Zn.ZnElement) secretInput.pointsVector.get(i));
            builder.putWitnessValue("newPoints_" + i, (Zn.ZnElement) secretInput.newPointsVector.get(i));
        }

        // Some asserts that might be useful for debugging:
        // assert pp.getNumEskDigits() == secretInput.eskStarUserDec.length();
        // assert secretInput.eskStarUser.equals(secretInput.eskStarUserDec.map((integer, znElement) -> znElement.mul(pp.getEskDecBase().pow(BigInteger.valueOf(integer)))).reduce(Zn.ZnElement::add));
        // assert commonInput.ctrace0.equals(pp.getW().pow(secretInput.rVector));
        // assert commonInput.ctrace1.equals(commonInput.ctrace0.pow(secretInput.esk).op(pp.getW().pow(secretInput.eskStarUserDec)));

        return builder.build();
    }

    @Override
    public ZnChallengeSpace getChallengeSpace(CommonInput commonInput) {
        return new ZnChallengeSpace(pp.getBg().getZn());
    }
}