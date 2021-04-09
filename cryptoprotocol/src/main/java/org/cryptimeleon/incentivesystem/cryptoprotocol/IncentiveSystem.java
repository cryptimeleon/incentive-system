package org.cryptimeleon.incentivesystem.cryptoprotocol;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinOutput;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinResponse;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessCommonInput;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessWitness;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * Contains all main algorithms of the incentive system according to 2020 incentive systems paper.
 */
@Value
@AllArgsConstructor
public class IncentiveSystem {

    // public parameters
    private IncentivePublicParameters pp;

    public static IncentivePublicParameters setup() {
        return Setup.trustedSetup(Setup.PRF_KEY_LENGTH);
    }

    public ProviderKeyPair generateProviderKeys() {
        return Setup.providerKeyGen(this.pp);
    }

    public UserKeyPair generateUserKeys() {
        return Setup.userKeyGen(this.pp);
    }




    /**
     * implementation of the Issue<->Join protocol
     */


    /**
     * functionality of the first part of the Issue algorithm of the Cryptimeleon incentive system
     * @param pp public parameters of the respective incentive system instance
     * @param pk provider public key of the provider the user interacts with
     * @param ukp user key pair
     * @return join request, i.e. object representing the first two messages in the Issue-Join protocol of the Cryptimeleon incentive system
     */
    public JoinRequest generateJoinRequest(IncentivePublicParameters pp, ProviderPublicKey pk, UserKeyPair ukp, ZnElement eskUsr, ZnElement dsrnd0, ZnElement dsrnd1, ZnElement z, ZnElement t, ZnElement u) {
        UserPublicKey upk = ukp.getPk();
        UserSecretKey usk = ukp.getSk();

        // TODO: generate random values needed for generation of fresh user token using PRF
        //  (currently, they are passed as method parameters until PRF stuff has been figured out),
        //  use Paul's stuff (see notes)

        // compute Pedersen commitment for user token
        RingElementVector exponents = new RingElementVector(usk.getUsk(), eskUsr, dsrnd0, dsrnd1, pp.getBg().getZn().getZeroElement(), z); // need to retrieve exponent from usk object; point count of 0 is reresented by zero in used Z_n
        GroupElement c0Pre = pk.getH().innerProduct(exponents).op(pp.getH7().pow(t)).pow(u);
        GroupElement c1Pre = pp.getG1Generator().pow(u);

        // compute NIZKP to prove well-formedness of token
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pk));
        CommitmentWellformednessCommonInput cwfCommon = new CommitmentWellformednessCommonInput(upk.getUpk(), c0Pre, c1Pre);
        CommitmentWellformednessWitness cwfWitness = new CommitmentWellformednessWitness(usk.getUsk(), eskUsr, dsrnd0, dsrnd1, z, t, u.inv());
        FiatShamirProof cwfProof = cwfProofSystem.createProof(cwfCommon, cwfWitness);

        // assemble and return join request object (commitment, proof of well-formedness)
        return new JoinRequest(c0Pre, c1Pre, cwfProof, cwfCommon);
    }

    /**
     * Implements the functionality of the Issue algorithm of the Cryptimeleon incentive system, i.e. handles a join request by signing the
     * included preliminary commitment after adding the provider's share for the tracking key esk.
     * @param pp public parameters of the respective incentive system instance
     * @param pkp key pair of the provider
     * @param jr join request to be handled
     * @return join response, i.e. object representing the third message in the Issue-Join protocol
     * @throws IllegalArgumentException indicating that the proof for commitment well-formedness was rejected
     */
    public JoinResponse generateJoinRequestResponse(IncentivePublicParameters pp, ProviderKeyPair pkp, JoinRequest jr) throws IllegalArgumentException {
        ProviderPublicKey pk = pkp.getPk();
        ProviderSecretKey sk = pkp.getSk();

        // read out parts of the precommitment and the commitment well-formedness proof from the join request object
        GroupElement c0Pre = jr.getPreCommitment0();
        GroupElement c1Pre = jr.getPreCommitment1();
        FiatShamirProof cwfProof = jr.getCwfProof();
        CommitmentWellformednessCommonInput cwfProofCommonInput = jr.getCwfProofCommonInput();

        // check commitment well-formedness proof for validity
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pk));
        if(!cwfProofSystem.checkProof(cwfProofCommonInput, cwfProof))
        {
            throw new IllegalArgumentException("The proof of the commitment being well-formed was rejected.");
        }

        // modify precommitment 0 using homorphism trick and randomly chosen exponent
        ZnElement eskProv = pp.getBg().getZn().getUniformlyRandomElement();
        GroupElement modifiedC0Pre = c0Pre.op(c1Pre.pow(sk.getQ().get(1).mul(eskProv)));

        // create certificate for modified pre-commitment vector
        SPSEQSignature cert = (SPSEQSignature) pp.getSpsEq().sign(sk.getSkSpsEq(), modifiedC0Pre, c1Pre); // first argument: signing keys, other arguments form the msg vector

        // assemble and return join response object
        return new JoinResponse(cert, eskProv);
    }

    /**
     * Implements the second part of the functionality of the Issue algorithm from the Cryptimeleon incentive system, i.e. computes the final user data
     * (token and corresponding certificate) from the signed preliminary token from the passed join request and response.
     * @param pp public parameters of the respective incentive system instance
     * @param pk public key of the provider the user interacted with
     * @param ukp key pair of the user handling the response
     * @param jReq the initial join request of the user handling the response to it
     * @param jRes join response to be handled
     * @return token containing 0 points
     */
    public Token handleJoinRequestResponse(IncentivePublicParameters pp, ProviderPublicKey pk, UserKeyPair ukp, JoinRequest jReq, JoinResponse jRes, ZnElement eskUsr, ZnElement dsrnd0, ZnElement dsrnd1, ZnElement z, ZnElement t, ZnElement u) {
        // extract relevant variables from join request and join response
        GroupElement c0Pre = jReq.getPreCommitment0();
        GroupElement c1Pre = jReq.getPreCommitment1();
        SPSEQSignature preCert = jRes.getPreCertificate();
        ZnElement eskProv = jRes.getEskProv();

        // re-compute modified pre-commitment for token
        GroupElement h2 = pk.getH().get(1);
        GroupElement modifiedC0Pre = c0Pre.op(h2.pow(u.mul(eskProv)));

        // change representation of token-certificate pair
        SPSEQSignature finalCert = (SPSEQSignature) pp.getSpsEq().chgRep(preCert, u.inv(), pk.getPkSpsEq()); // adapt signature
        GroupElement finalCommitment0 = modifiedC0Pre.pow(u.inv()); // need to adapt message manually (entry by entry), used equivalence relation is R_exp
        GroupElement finalCommitment1 = c1Pre.pow(u.inv());

        // assemble and return token
        ZnElement esk = eskUsr.add(eskProv);
        Zn usedZn = pp.getBg().getZn();
        Token token = new Token(finalCommitment0, finalCommitment1, esk, dsrnd0, dsrnd1, z, t, usedZn.getZeroElement(), finalCert);
        return token;
    }

    /**
     * end of the implementation of the Issue<->Join protocol
     */



    /**
     * implementation of the Credit<->Earn protocol
     */

    void generateEarnRequest(Token token, UserKeyPair userKeyPair, ProviderPublicKey providerPublicKey) {
        // change representative call to blind commitment and certificate
        var pk = userKeyPair.getPk();
        var sk = userKeyPair.getSk();

        Zn usedZn = pp.getBg().getZn(); // draw blinding value
        var s = usedZn.getUniformlyRandomNonzeroElement(); // s cannot be zero since we need to compute its inverse to unblind the signature

        var certificate = token.getSignature();
        var provSPSPk = providerPublicKey.getPkSpsEq(); // store SPS EQ verification key from provider public key
        var blindedCommitment = token.getCommitment().pow(s); // computing another representative with blinding randomness (the implemented SPS-EQ is over R_exp)
        var blindedCertificate = (SPSEQSignature) pp.getSpsEq().chgRep(certificate, s, provSPSPk); // note: in contrast to formal specification, chgrep only takes three arguments (no message) and thus only updates the signature
    }

    void generateEarnRequestResponse() {
    }

    void handleEarnRequestResponse() {
    }

    /**
     * end of the implementation of the Credit<->Earn protocol
     */




    /**
     * implementation of the Deduct<->Spend protocol
     */

    void generateSpendRequest() {
    }

    void generateSpendRequestResponse() {
    }

    void handleSpendRequestResponse() {
    }

    /**
     * end of the implementation of the Deduct<->Spend protocol
     */




    /**
     * methods for offline double-spending detection
     */

    void link() {
    }

    void verifyDS() {
    }

    void trace() {
    }

    /**
     * end of methods for offline double-spending detection
     */
}
