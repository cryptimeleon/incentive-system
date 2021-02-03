package de.upb.crypto.incentive.cryptoprotocol.protocols.user;

import de.upb.crypto.craco.sig.sps.eq.SPSEQSignature;
import de.upb.crypto.craco.sig.sps.eq.SPSEQVerificationKey;
import de.upb.crypto.incentive.cryptoprotocol.interfaces.user.EarnInterface;
import de.upb.crypto.incentive.cryptoprotocol.model.PublicParameters;
import de.upb.crypto.incentive.cryptoprotocol.model.Token;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.user.UserSecretKey;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.structures.rings.zn.Zn;
import de.upb.crypto.math.structures.rings.zn.Zn.ZnElement;

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
