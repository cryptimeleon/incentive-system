package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.common.PublicParameters;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/*
 * Contains all main algorithms of the incentive system according to 2020 incentive systems paper.
 */
public class IncentiveSystem {

    // public parameters
    IncentivePublicParameters pp;

    public IncentiveSystem(IncentivePublicParameters pp) {
        this.pp = pp;
    }

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

    public JoinRequest generateJoinRequest(IncentivePublicParameters pp, ProviderPublicKey pk, UserPublicKey upk, UserSecretKey usk) {
        // generate random values needed for generation of fresh user token
        Zn usedZn = pp.getBg().getZn();
        ZnElement eskUsr  = usedZn.getUniformlyRandomElement(); // user's share of the encryption secret key for the tracing information's encryption
        ZnElement dsrnd0  = usedZn.getUniformlyRandomElement(); // randomness for the first challenge generation in double-spending protection
        ZnElement dsrnd1  = usedZn.getUniformlyRandomElement(); // randomness for the second challenge generation in double-spending protection
        ZnElement z  = usedZn.getUniformlyRandomElement(); // blinding randomness needed to make (C^u, g^u) uniformly random
        ZnElement t  = usedZn.getUniformlyRandomElement(); // blinding randomness needed for special DDH trick in a proof
        ZnElement u  = usedZn.getUniformlyRandomElement(); //

        // compute Pedersen commitment for user token
        RingElementVector exponents = new RingElementVector(usk.getUsk(), eskUsr, dsrnd0, dsrnd1, usedZn.getZeroElement(), z); // need to retrieve exponent from usk object; point count of 0 is reresented by zero in used Z_n
        GroupElement preCommitment = pk.getH().pow(exponents) // need to turn this into a single element // .op(
                pp.getH7().pow(t)
        );


        // compute NIZKP to prove well-formedness of token

        // assemble and return join request object (commitment, proof of well-formedness)
        return null;
    }

    void generateJoinRequestResponse() {
    }

    void handleJoinRequestResponse() {
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
