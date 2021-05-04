package org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.ZnChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.LinearStatementFragment;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendThenDelegateFragment;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.math.structures.cartesian.GroupElementExpressionVector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * A protocol for proof of the well-formedness of the preliminary commitment in the initial user token as defined in the Issue<->Join protocol of
 * the Cryptimeleon incentive system.
 *
 * In particular, it proves knowledge of (usk, t, z, 1/u, eskUsr, dsrnd0, dsrnd1) in Z_p^7 s.t.
 * I    upk = w^usk
 * II   ( C^pre_0 ) ^ 1/u = h1^usk * h2^esk_usr * h3^dsrnd0 * h4^dsrnd1 * h6^z * h7^t
 * III  g1 = ( C^pre_1 ) ^ 1/u
 *
 * Note that instead of u, 1/u is used to avoid implementation of double exponent proofs.
 *
 * The common input for the interactive protocol for that proof is split into two parts:
 * the static part consists of the variables that are the same for every interaction, namely
 * w, h1, h2, h3, h4, h6, h7, g1 .
 * These values are stored in the CommitmentWellformednessProof object.
 * On the other hand, there is the non-static part of the common input which comprises
 * upk, c0Pre, c1Pre
 */
@Value
@AllArgsConstructor
public class CommitmentWellformednessProtocol extends DelegateProtocol {
    private IncentivePublicParameters pp; // public parameters of the respective incentive system
    private ProviderPublicKey pk; // provider public key used for in an instance of the protocol

    /**
     * specifies verifier side of the protocol (i.e. what verifiers with what properties shall a prover interacting with that verifier prove knowledge of).
     * @param commonInput (non-static part of) common input
     * @param builder object used to construct the protocol from statement objects
     * @return instance holding all information about the verifier side of the protocol
     */
    @Override
    protected SendThenDelegateFragment.SubprotocolSpec provideSubprotocolSpec(CommonInput commonInput, SendThenDelegateFragment.SubprotocolSpecBuilder builder) {
        // read out all values from provider public key and public parameters that are used in the statements to prove
        var w = this.pp.getW();
        var h1 = this.pk.getH().get(0); // note: h's have 1-based index in paper, 0-based in GroupElementVector
        var h2 = this.pk.getH().get(1);
        var h3 = this.pk.getH().get(2);
        var h4 = this.pk.getH().get(3);
        var h6 = this.pk.getH().get(5);
        var h7 = this.pp.getH7();
        var g1 = this.pp.getG1Generator();



        // register all witnesses that should be proven knowledge about to the builder (as variables, this is needed to define expression objects that resemble the statements)
        Zn usedZn = this.pp.getBg().getZn();
        var uskVar = builder.addZnVariable("usk", usedZn);
        var tVar = builder.addZnVariable("t", usedZn);
        var zVar = builder.addZnVariable("z", usedZn);
        var uInverseVar = builder.addZnVariable("uInverse", usedZn);
        var eskUsrVar = builder.addZnVariable("eskUsr", usedZn);
        var dsrnd0Var = builder.addZnVariable("dsrnd0", usedZn);
        var dsrnd1Var = builder.addZnVariable("dsrnd1", usedZn);



        // create statement objects for all three statements that shall be proven

        CommitmentWellformednessCommonInput castedCommonInput = (CommitmentWellformednessCommonInput) commonInput;

        // upk
        var upk = castedCommonInput.getUpk();
        var keyDLogStatement = w.pow(uskVar).isEqualTo( upk );

        // first group element in the commitment
        var c0Pre = castedCommonInput.getC0Pre();
        var firstGeStatement = h1.pow(uskVar).op(
                h2.pow(eskUsrVar).op(
                        h3.pow(dsrnd0Var).op(
                                h4.pow(dsrnd1Var).op(
                                        h6.pow(zVar).op(
                                                h7.pow(tVar)
                                        )
                                )
                        )
                )
        ).isEqualTo(c0Pre.pow(uInverseVar));

        // second group element in the commitment
        var c1Pre = castedCommonInput.getC1Pre();
        var secondGeStatement = c1Pre.pow(uInverseVar).isEqualTo(g1);

        // done creating statement objects



        // add fragments for those statements
        builder.addSubprotocol("userKeyWellFormed", new LinearStatementFragment(keyDLogStatement));
        builder.addSubprotocol("firstElementWellFormed", new LinearStatementFragment(firstGeStatement));
        builder.addSubprotocol("secondElementWellFormed", new LinearStatementFragment(secondGeStatement));



        // build the verifier side of subprotocol using the passed builder
        return builder.build();
    }

    /**
     * specifies prover side of the protocol (i.e. which witnesses it shall use).
     * Specified witnesses must match variables added when defining verifier side of protocol.
     * @param commonInput (non-static) part of common input
     * @param secretInput
     * @param builder
     * @return
     */
    @Override
    protected SendThenDelegateFragment.ProverSpec provideProverSpecWithNoSendFirst(CommonInput commonInput, SecretInput secretInput, SendThenDelegateFragment.ProverSpecBuilder builder) {
        CommitmentWellformednessWitness castedSecretInput = (CommitmentWellformednessWitness) secretInput;

        // set up witnesses prover shall use
        builder.putWitnessValue("usk", castedSecretInput.getUsk());
        builder.putWitnessValue("t", castedSecretInput.getT());
        builder.putWitnessValue("z", castedSecretInput.getZ());
        builder.putWitnessValue("uInverse", castedSecretInput.getUInverse());
        builder.putWitnessValue("eskUsr", castedSecretInput.getEskUsr());
        builder.putWitnessValue("dsrnd0", castedSecretInput.getDsrnd0());
        builder.putWitnessValue("dsrnd1", castedSecretInput.getDsrnd1());

        // build the prover side of subprotocol using the passed builder
        return builder.build();
    }

    @Override
    public ZnChallengeSpace getChallengeSpace(CommonInput commonInput)
    {
        return new ZnChallengeSpace(this.pp.getBg().getZn());
    }
}
