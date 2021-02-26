package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/*
 * Contains all main algorithms of the incentive system.
 */
public class IncentiveSystem {

    // Public parameters
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


    // This is highly WIP
    void generateJoinRequest() {
    }

    void generateJoinRequestResponse() {
    }

    void handleJoinRequestResponse() {
    }

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

    void generateSpendRequest() {
    }

    void generateSpendRequestResponse() {
    }

    void handleSpendRequestResponse() {
    }

    void link() {
    }

    void verifyDS() {
    }

    void trace() {
    }
}
