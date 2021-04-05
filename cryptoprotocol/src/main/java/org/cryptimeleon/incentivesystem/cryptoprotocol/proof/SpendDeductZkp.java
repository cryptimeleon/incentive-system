package org.cryptimeleon.incentivesystem.cryptoprotocol.proof;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearExponentStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendThenDelegateFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.setmembership.SetMembershipFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.setmembership.SmallerThanPowerFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.variables.SchnorrZnVariable;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.math.expressions.exponent.ExponentConstantExpr;
import org.cryptimeleon.math.expressions.exponent.ExponentMulExpr;
import org.cryptimeleon.math.expressions.exponent.ExponentSumExpr;
import org.cryptimeleon.math.expressions.group.GroupElementExpression;
import org.cryptimeleon.math.structures.cartesian.GroupElementExpressionVector;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class SpendDeductZkp extends DelegateProtocol {

    private final IncentivePublicParameters pp;
    private final GroupElement g1;
    private final Zn zn;
    private final Group groupG1;
    private final ProviderPublicKey providerPublicKey;

    public SpendDeductZkp(IncentivePublicParameters incentivePublicParameters, ProviderPublicKey providerPublicKey) {
        this.providerPublicKey = providerPublicKey;
        this.pp = incentivePublicParameters;
        this.g1 = incentivePublicParameters.getG1();
        this.zn = incentivePublicParameters.getBg().getZn();
        this.groupG1 = incentivePublicParameters.getBg().getG1();
    }


    @Override
    protected SendThenDelegateFragment.SubprotocolSpec provideSubprotocolSpec(CommonInput pCommonInput, SendThenDelegateFragment.SubprotocolSpecBuilder builder) {
        var commonInput = (SpendDeductCommonInput) pCommonInput;
        var H = new GroupElementExpressionVector(providerPublicKey.getH().pad(pp.getH7(), 7).map(GroupElement::expr));
        var w = pp.getW();

        // Variables to use
        var eskVar = builder.addZnVariable("esk", zn);
        var uskVar = builder.addZnVariable("usk", zn);
        var dsrnd0Var = builder.addZnVariable("dsrnd0", zn);
        var dsrnd1Var = builder.addZnVariable("dsrnd1", zn);
        var dsrndStar0Var = builder.addZnVariable("dsrndStar0", zn);
        var dsrndStar1Var = builder.addZnVariable("dsrndStar1", zn);
        var vVar = builder.addZnVariable("v", zn);
        var zVar = builder.addZnVariable("z", zn);
        var tVar = builder.addZnVariable("t", zn);
        var zStarVar = builder.addZnVariable("zStar", zn);
        var tStarVar = builder.addZnVariable("tStar", zn);
        var uStarVar = builder.addZnVariable("uStar", zn);
        var uStarInverseVar = builder.addZnVariable("uStarInverse", zn);
        var eskDecVarVector = new SchnorrZnVariable[pp.getNumEskDigits()];
        for (int i = 0; i < pp.getNumEskDigits(); i++) {
            eskDecVarVector[i] = builder.addZnVariable("eskStarUserDec_" + i, zn);
        }

        // c0=usk*gamma+dsrnd0
        var c0statement = new ExponentSumExpr(new ExponentMulExpr(uskVar, new ExponentConstantExpr(commonInput.gamma)), dsrnd0Var);
        builder.addSubprotocol("c0=usk*gamma+dsrnd0", new LinearExponentStatementFragment(c0statement.isEqualTo(commonInput.c0), zn));

        // c1=esk*gamma+dsrnd1
        var c1statement = new ExponentSumExpr(new ExponentMulExpr(eskVar, new ExponentConstantExpr(commonInput.gamma)), dsrnd1Var);
        builder.addSubprotocol("c1=esk*gamma+dsrnd1", new LinearExponentStatementFragment(c1statement.isEqualTo(commonInput.c1), zn));

        // dsid=w^esk
        var dsidEskStatement = w.pow(eskVar).isEqualTo(commonInput.dsid);
        builder.addSubprotocol("dsid=w^esk", new LinearStatementFragment(dsidEskStatement));

        // C=(H.pow(usk, esk, dsrnd_0, dsrnd_1, v, z, t),g_1) split into two subprotocols
        var commitmentC0Statement = H.pow(new Vector<>(
                uskVar,
                eskVar,
                dsrnd0Var,
                dsrnd1Var,
                vVar,
                zVar,
                tVar
        )).reduce(GroupElementExpression::op).isEqualTo(commonInput.commitmentC0);
        builder.addSubprotocol("C0", new LinearStatementFragment(commitmentC0Statement));
        builder.addSubprotocol("C1", new LinearStatementFragment(commonInput.commitmentC1.expr().isEqualTo(pp.getG1())));

        // C=(H.pow(usk, \sum_i=0^k[esk^*_(usr,i) * base^i], dsrnd^*_0, dsrnd^*_1, v-k, z^*, t^*), g_1^(u^*)) split into two subprotocols
        // We use the sum to combine the esk^*_usr = \sum proof with the C=.. proof
        var exponents = new Vector<>(uskVar, BigInteger.ZERO, dsrndStar0Var, dsrndStar1Var, vVar.sub(zn.valueOf(commonInput.k)), zStarVar, tStarVar); // 0 because h_2^(esk^*_usr) is handled separately
        GroupElementExpression eskPart = groupG1.getNeutralElement().expr();
        for (int i = 0; i< pp.getNumEskDigits(); i++) {
            eskPart = eskPart.op(H.get(1).pow(eskDecVarVector[i].mul(pp.getEskDecBase().pow(BigInteger.valueOf(i)))));
        }

        var cPre0Statement = H.pow(exponents).reduce(GroupElementExpression::op).op(eskPart).isEqualTo(commonInput.c0Pre.pow(uStarInverseVar));
        builder.addSubprotocol("C0Pre", new LinearStatementFragment(cPre0Statement));
        builder.addSubprotocol("C1Pre", new LinearStatementFragment(commonInput.c1Pre.expr().isEqualTo(pp.getG1().pow(uStarVar))));
        builder.addSubprotocol("u^*-inverse", new LinearStatementFragment(commonInput.c1Pre.pow(uStarInverseVar).isEqualTo(pp.getG1()))); // Prove that the inverse is actually the inverse

        // esk^*_(usr,i)\in[base]
        for (int i = 0; i< pp.getNumEskDigits(); i++) {
            builder.addSubprotocol("eskDigitSetMembership_" + i, new SetMembershipFragment(pp.getEskBaseSetMembershipPublicParameters(), eskDecVarVector[i]));
        }

        // v >= k (I have more points than required)
        // We prove that v-k\in[0,eskDecBase^{maxPointBasePower+1}-1]
        builder.addSubprotocol("v>=k", new SmallerThanPowerFragment(vVar.sub(new ExponentConstantExpr(commonInput.k)), pp.getEskDecBase().getInteger().intValue(), pp.getMaxPointBasePower(), pp.getEskBaseSetMembershipPublicParameters()));

        // ctrace=(w^{r_i} ,{w^{r_i}}^{esk}*w^{esk^*_{usr,i}}) for all i\in[p]
        for (int i = 0; i< pp.getNumEskDigits(); i++) {
            // builder.addSubprotocol("ctrace_0_" + i, new LinearStatementFragment());
            // TODO
        }

        return builder.build();
    }

    @Override
    protected SendThenDelegateFragment.ProverSpec provideProverSpecWithNoSendFirst(CommonInput pCommonInput, SecretInput pSecretInput, SendThenDelegateFragment.ProverSpecBuilder builder) {
        var commonInput = (SpendDeductCommonInput) pCommonInput;
        var secretInput = (SpendDeductWitnessInput) pSecretInput;

        // Add variables to witness
        builder.putWitnessValue("esk", secretInput.esk);
        builder.putWitnessValue("usk", secretInput.usk);
        builder.putWitnessValue("dsrnd0", secretInput.dsrnd0);
        builder.putWitnessValue("dsrnd1", secretInput.dsrnd1);
        builder.putWitnessValue("dsrndStar0", secretInput.dsrndStar0);
        builder.putWitnessValue("dsrndStar1", secretInput.dsrndStar1);
        builder.putWitnessValue("v", secretInput.v);
        builder.putWitnessValue("z", secretInput.z);
        builder.putWitnessValue("t", secretInput.t);
        builder.putWitnessValue("zStar", secretInput.zStar);
        builder.putWitnessValue("tStar", secretInput.tStar);
        builder.putWitnessValue("uStar", secretInput.uStar);
        builder.putWitnessValue("uStarInverse", secretInput.uStar.inv());

        assert pp.getNumEskDigits() == secretInput.eskStarUserDec.length();
        for (int i = 0; i < pp.getNumEskDigits(); i++) {
            builder.putWitnessValue("eskStarUserDec_" + i, secretInput.eskStarUserDec.get(i));
        }

        // Ensure that decomposition works
        assert secretInput.eskStarUser.equals(secretInput.eskStarUserDec.map((integer, znElement) -> znElement.mul(pp.getEskDecBase().pow(BigInteger.valueOf(integer)))).reduce(Zn.ZnElement::add));

        return builder.build();
    }

    @Override
    public BigInteger getChallengeSpaceSize() {
        return groupG1.size();
    }
}