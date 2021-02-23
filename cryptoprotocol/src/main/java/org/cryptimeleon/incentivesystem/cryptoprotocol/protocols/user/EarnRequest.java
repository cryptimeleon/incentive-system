package org.cryptimeleon.incentivesystem.cryptoprotocol.protocols.user;

import org.cryptimeleon.incentivesystem.cryptoprotocol.model.PublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQVerificationKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.interfaces.user.EarnInterface;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * Class representing an earn request created by a user of the incentive system.
 * Provides functionality to initialize the request (i.e. preparing all data that needs to be send to the provider on making the request)
 * as well as handling the providers response.
 * Note that not all fields of this class are part of its serialized representation (for privacy reasons).
 */
public class EarnRequest implements EarnInterface, Representable {
    @Represented
    private PublicParameters pp;
    @Represented
    private ProviderPublicKey pk;
    private UserSecretKey usk;
    private long k; // TODO: does k even need
    private Token token; // TODO: is it a problem if token is unserialized? -> shouldn't be because serialization is no encryption

    @Represented
    private GroupElement blindedCommitment; // the blinded commitment sent in the earn request
    @Represented
    private SPSEQSignature blindedCertificate;
    private ZnElement s; // blinding randomness, maybe easier to retry requests if the randomness is saved in the requests instance

    /**
     * @param k earn amount, name taken from 2020 inc sys paper
     */
    public EarnRequest(PublicParameters pp, ProviderPublicKey pk, UserSecretKey usk, long k, Token token) {
        Zn usedZn = pp.getBg().getZn();
        // draw blinding value
        this.s = usedZn.getUniformlyRandomNonzeroElement(); // s cannot be zero since we need to compute its inverse to unblind the signature

        // rest of object vars: straightforward assignment
        this.pp = pp;
        this.pk = pk;
        this.usk = usk;
        this.k = k;
        this.token = token;
    }

    public String generateSerializedEarnRequest() {
        // change representative call to blind commitment and certificate
        SPSEQSignature certificate = this.token.getCertificate();
        SPSEQVerificationKey provSPSPk = this.pk.getPkSpsEq(); // store SPS EQ verification key from provider public key
        this.blindedCommitment = this.token.getToken().pow(s); // computing another representative with blinding randomness (the implemented SPS-EQ is over R_exp)
        this.blindedCertificate = (SPSEQSignature) pp.getSpsEq().chgRep(certificate, this.s, provSPSPk); // note: in contrast to formal specification, chgrep only takes three arguments (no message) and thus only updates the signature

        // serialize request object by computing representation
        Representation requestRepresentation = getRepresentation();

        // return string representation
        return requestRepresentation.str().get();
    }

    public String handleSerializedCreditResponse(String serializedCreditResponse) {
        // deserialize response

        // unblind signature

        // return token TODO: String a good return type?
        return null;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
