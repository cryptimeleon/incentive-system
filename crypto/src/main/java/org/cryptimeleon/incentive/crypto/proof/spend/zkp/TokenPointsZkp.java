package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.ZnChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;
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
 * A zero knowledge proof over a token's points for range proofs.
 */
class TokenPointsZkp extends DelegateProtocol {

    // Public parameters
    final IncentivePublicParameters pp;
    // Proof that lowerLimits[i] <= points[i] <= upperLimits[i] for all i
    final Vector<BigInteger> lowerLimits; // null means no limit
    final Vector<BigInteger> upperLimits; // null means no limit
    final ProviderPublicKey providerPublicKey;
    final PromotionParameters promotionParameters;

    public TokenPointsZkp(IncentivePublicParameters pp, Vector<BigInteger> lowerLimits, Vector<BigInteger> upperLimits, ProviderPublicKey providerPublicKey, PromotionParameters promotionParameters) {
        this.pp = pp;
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
        this.providerPublicKey = providerPublicKey;
        this.promotionParameters = promotionParameters;
    }

    @Override
    protected SendThenDelegateFragment.SubprotocolSpec provideSubprotocolSpec(CommonInput pCommonInput, SendThenDelegateFragment.SubprotocolSpecBuilder builder) {
        var commonInput = (SpendDeductZkpCommonInput) pCommonInput;
        var H = new GroupElementExpressionVector(providerPublicKey.getH(pp, promotionParameters).map(GroupElement::expr));
        var zn = pp.getBg().getZn();

        // Variables to use
        var uskVar = builder.addZnVariable("usk", zn);
        var dsrndVar = builder.addZnVariable("dsrnd", zn);
        var zVar = builder.addZnVariable("z", zn);
        var tVar = builder.addZnVariable("t", zn);
        var pointsVector = ExponentExpressionVector.generate(i -> builder.addZnVariable("points_" + i, zn), promotionParameters.getPointsVectorSize());

        // C_0=H.pow(t, usk, dsid, dsrnd, z, points)
        var commitmentC0Statement = H.innerProduct(
                ExponentExpressionVector.of(tVar, uskVar, commonInput.dsid, dsrndVar, zVar).concatenate(pointsVector.map(e -> e))
        ).isEqualTo(commonInput.commitmentC0);
        builder.addSubprotocol("C0", new LinearStatementFragment(commitmentC0Statement));

        // Add range proofs
        for (int i = 0; i < promotionParameters.getPointsVectorSize(); i++) {
            if (lowerLimits.get(i) != null) {
                builder.addSubprotocol(
                        "pointsVector[" + i + "]>=lowerLimits[" + i + "]",
                        new SmallerThanPowerFragment(
                                pointsVector.get(i).sub(lowerLimits.get(i).intValue()),
                                pp.getRangeProofBase().asInteger().intValue(),
                                pp.getMaxPointBasePower(),
                                pp.getSetMembershipPublicParameters()
                        )
                );
            }
            if (upperLimits.get(i) != null) {
                builder.addSubprotocol(
                        "pointsVector[" + i + "]<=upperLimits[" + i + "]",
                        new SmallerThanPowerFragment(
                                (new ExponentConstantExpr(upperLimits.get(i))).sub(pointsVector.get(i)),
                                pp.getRangeProofBase().asInteger().intValue(),
                                pp.getMaxPointBasePower(),
                                pp.getSetMembershipPublicParameters()
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
        builder.putWitnessValue("usk", secretInput.usk);
        builder.putWitnessValue("dsrnd", secretInput.dsrnd);
        builder.putWitnessValue("z", secretInput.z);
        builder.putWitnessValue("t", secretInput.t);

        for (int i = 0; i < promotionParameters.getPointsVectorSize(); i++) {
            builder.putWitnessValue("points_" + i, (Zn.ZnElement) secretInput.pointsVector.get(i));
        }

        return builder.build();
    }

    @Override
    public ZnChallengeSpace getChallengeSpace(CommonInput commonInput) {
        return new ZnChallengeSpace(pp.getBg().getZn());
    }
}
