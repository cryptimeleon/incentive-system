package de.upb.crypto.incentive.cryptoprotocol.model;


import de.upb.crypto.craco.common.GroupElementPlainText;
import de.upb.crypto.craco.sig.interfaces.VerificationKey;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignature;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignatureScheme;
import de.upb.crypto.incentive.cryptoprotocol.exceptions.SPSEQException;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.cartesian.GroupElementVector;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.net.PortUnreachableException;


/**
 * data holding class representing a token from a mathematical point of view (meaning: as a bunch of group elements and exponents).
 * serialized representation of token does not contain secret exponents and token plain text (latter can be computed trivially).
 */
public class Token implements Representable {
    @Represented
    private PublicParameters pp; // public parameters for the inc sys instance in which the token was created

    @Represented
    private GroupElement token; // the Pedersen commitment computed from the bases and the exponents, representing the actual token
    private GroupElementPlainText tokenPlainText; // plaintext version of token, needed for compliance with verification method
    @Represented
    private SPSEQSignature certificate; // the SPS-EQ certifying the commitment as well-formed and valid
    @Represented
    private VerificationKey verificationKey; // the SPS-EQ pk used to check validity of token-certificate pair

    private GroupElementVector commitmentBases; // array of the 7 group elements (h_i) needed to form the Pedersen commitment
    private ZnElement userSecretKey; // secret key of the user the token belongs to
    private ZnElement encryptionSecretKey; // secret key used for the ElGamal encryption in the Spend algorithm
    private ZnElement doubleSpendRandomness0; // randomness used for the first challenge generation in double spending protection
    private ZnElement doubleSpendRandomness1; // randomness used for the second challenge generation in double spending protection
    private ZnElement points; // number of points that the token currently stores (initially 0)
    private ZnElement z, t; // values for blinding the token group element

    /**
     * standard constructor, intializes an empty token, meaning a token storing 0 points represented as a Pedersen commitment.
     * Note that certificate is not set since a token is usually not certified upon creation.
     *
     * @param pp     public parameters
     * @param vKey   verification key
     * @param pk     provider public key
     * @param USK    secret key of user
     * @param esk    ElGamal secret key
     * @param dsrnd0 double spending randomness
     * @param dsrnd1 other double spending randomness
     */
    public Token(PublicParameters pp, VerificationKey vKey, ProviderPublicKey pk, ZnElement USK, ZnElement esk, ZnElement dsrnd0, ZnElement dsrnd1, ZnElement z, ZnElement t) throws IllegalArgumentException {
        // retrieve commitment base array h from passed provider public key
        GroupElementVector h = pk.getH();
        this.commitmentBases = h;

        // initializing respective object variables with other parameters
        Zn myRemainderClassRing = pp.getBg().getZn(); // needed to retrieve correct zero element to compute point count
        this.verificationKey = vKey;
        this.userSecretKey = USK;
        this.encryptionSecretKey = esk;
        this.doubleSpendRandomness0 = dsrnd0;
        this.doubleSpendRandomness1 = dsrnd1;
        this.points = myRemainderClassRing.getZeroElement(); // initially, token contains no points
        this.z = z;
        this.t = t;

        // compute the token-representing Pedersen commitment for the first time
        this.recomputeCommitment();
    }

    /**
     * updates token with the passed values and recomputes commitment
     */
    private void updateToken(ZnElement USK, ZnElement esk, ZnElement dsrnd0, ZnElement dsrnd1, ZnElement v, ZnElement z, ZnElement t) {
        this.userSecretKey = USK;
        this.encryptionSecretKey = esk;
        this.doubleSpendRandomness0 = dsrnd0;
        this.doubleSpendRandomness1 = dsrnd1;
        this.points = v;
        this.z = z;
        this.t = t;

        this.recomputeCommitment();
    }

    /**
     * update point count and recompute commitment
     *
     * @param v new point count
     */
    private void updatePoints(ZnElement v) {
        this.points = v;

        this.recomputeCommitment();
    }

    /**
     * recomputes the Pedersen commitment representing the token from the group elements and exponents (and also updates its plaintext representation)
     * (see chapter 4 of 2020 incentive systems paper)
     */
    private void recomputeCommitment() {
        // note that indices of bases are off by one wrt paper (zero-based indexing)
        this.token = this.commitmentBases.get(0).pow(this.userSecretKey)
                .op(this.commitmentBases.get(1).pow(this.encryptionSecretKey))
                .op(this.commitmentBases.get(2).pow(this.doubleSpendRandomness0))
                .op(this.commitmentBases.get(3).pow(this.doubleSpendRandomness1))
                .op(this.commitmentBases.get(4).pow(this.points))
                .op(this.commitmentBases.get(5).pow(this.z))
                .op(this.commitmentBases.get(6).pow(this.t));

        tokenPlainText = new GroupElementPlainText(this.token);
    }


    /**
     * sets the certificate for the token.
     *
     * @param cert new certificate
     */
    public void setCertificate(SPSEQSignature cert) throws SPSEQException {
        this.certificate = cert;

        SPSEQSignatureScheme signatureScheme = pp.getSpsEq();

        // immediately throw exception if signature not valid for token group element
        if (!signatureScheme.verify(this.tokenPlainText, this.certificate, this.verificationKey)) // token plaintext needed for verification (API reasons)
        {
            throw new SPSEQException("token was associated with an invalid certificate");
        }
    }

    /**
     * computes "dumb token" from mathematical representation of token
     *
     * @return serialized version of token
     */
    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public GroupElement getToken() {
        return this.token;
    }

    public SPSEQSignature getCertificate() {
        return this.certificate;
    }
}

