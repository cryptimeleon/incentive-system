package org.cryptimeleon.incentivesystem.cryptoprotocol.proof;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearExponentStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendThenDelegateFragment;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.math.expressions.exponent.ExponentConstantExpr;
import org.cryptimeleon.math.expressions.exponent.ExponentMulExpr;
import org.cryptimeleon.math.expressions.exponent.ExponentSumExpr;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class SpendDeductZkp extends DelegateProtocol {

    private final IncentivePublicParameters incentivePublicParameters;
    private final GroupElement g1;
    private final Zn zn;
    private final Group groupG1;

    public SpendDeductZkp(IncentivePublicParameters incentivePublicParameters) {
        this.incentivePublicParameters = incentivePublicParameters;
        this.g1 = incentivePublicParameters.getG1();
        this.zn = incentivePublicParameters.getBg().getZn();
        this.groupG1 = incentivePublicParameters.getBg().getG1();
    }


    @Override
    protected SendThenDelegateFragment.SubprotocolSpec provideSubprotocolSpec(CommonInput pCommonInput, SendThenDelegateFragment.SubprotocolSpecBuilder builder) {
        var commonInput = (SpendDeductCommonInput) pCommonInput;

        // Variables to use
        var eskVar = builder.addZnVariable("esk", zn);
        var uskVar = builder.addZnVariable("usk", zn);
        var dsrnd0Var = builder.addZnVariable("dsrnd0", zn);
        var dsrnd1Var = builder.addZnVariable("dsrnd1", zn);
        var dsrndStar0Var = builder.addZnVariable("dsrndStar0", zn);
        var dsrndStar1Var = builder.addZnVariable("dsrndStar1", zn);

        // c0=usk*gamma+dsrnd0
        var c0statement = new ExponentSumExpr(new ExponentMulExpr(uskVar, new ExponentConstantExpr(commonInput.gamma)), dsrnd0Var);
        builder.addSubprotocol("c0=usk*gamma+dsrnd0", new LinearExponentStatementFragment(c0statement.isEqualTo(commonInput.c0), zn));

        // c1=esk*gamma+dsrnd1
        var c1statement = new ExponentSumExpr(new ExponentMulExpr(eskVar, new ExponentConstantExpr(commonInput.gamma)), dsrnd1Var);
        builder.addSubprotocol("c1=esk*gamma+dsrnd1", new LinearExponentStatementFragment(c1statement.isEqualTo(commonInput.c1), zn));

        // dsid=w^esk
        var dsidEskStatement = commonInput.w.pow(eskVar).isEqualTo(commonInput.dsid);
        builder.addSubprotocol("dsid=w^esk", new LinearStatementFragment(dsidEskStatement));

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

        return builder.build();
    }

    @Override
    public BigInteger getChallengeSpaceSize() {
        return groupG1.size();
    }
}