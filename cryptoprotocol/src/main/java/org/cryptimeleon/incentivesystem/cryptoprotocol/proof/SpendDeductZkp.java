package org.cryptimeleon.incentivesystem.cryptoprotocol.proof;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendThenDelegateFragment;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
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

        // Statements to prove
        var dsidEskStatement = commonInput.w.pow(eskVar).isEqualTo(commonInput.dsid);

        // Adding statements to proof
        builder.addSubprotocol("dsid=w^esk", new LinearStatementFragment(dsidEskStatement));

        return builder.build();
    }

    @Override
    protected SendThenDelegateFragment.ProverSpec provideProverSpecWithNoSendFirst(CommonInput pCommonInput, SecretInput pSecretInput, SendThenDelegateFragment.ProverSpecBuilder builder) {
        var commonInput = (SpendDeductCommonInput) pCommonInput;
        var secretInput = (SpendDeductWitnessInput) pSecretInput;

        // Add variables to witness
        builder.putWitnessValue("esk", secretInput.esk);

        return builder.build();
    }

    @Override
    public BigInteger getChallengeSpaceSize() {
        return groupG1.size();
    }
}