package de.upb.crypto.incentive.cryptoprotocol.model;


import de.upb.crypto.craco.common.GroupElementPlainText;
import de.upb.crypto.craco.sig.interfaces.VerificationKey;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignature;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignatureScheme;
import de.upb.crypto.incentive.cryptoprotocol.exceptions.PedersenException;
import de.upb.crypto.incentive.cryptoprotocol.exceptions.SPSEQException;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;


/**
 * class representing a token from a mathematical point of view (meaning: as a bunch of group elements and exponents modulo p from which you can compute a Pedersen commitment)
 * serialized representation of token does not contain secret exponents and token plain text (latter can be computed trivially)
 */
public class Token implements Representable
{
    @Represented
    private Zn myRemainderClassRing; // the remainder class ring the token's exponents live in
    @Represented
    private SPSEQSignatureScheme signatureScheme; // the instance of the signature scheme that is used to certify tokens

    @Represented
    private GroupElement token; // the Pedersen commitment computed from the bases and the exponents, representing the actual token
    private GroupElementPlainText tokenPlainText; // plaintext version of token, needed for compliance with verification method
    @Represented
    private SPSEQSignature certificate; // the SPS-EQ certifying the commitment as well-formed and valid
    @Represented
    private VerificationKey verificationKey; // the SPS-EQ pk used to check validity of token-certificate pair

    private GroupElement[] commitmentBases; // array of the 7 group elements (h_i) needed to form the Pedersen commitment
    private ZnElement userSecretKey; // secret key of the user the token belongs to
    private ZnElement encryptionSecretKey; // secret key used for the ElGamal encryption in the Spend algorithm
    private ZnElement doubleSpendRandomness0; // randomness used for the first challenge generation in double spending protection
    private ZnElement doubleSpendRandomness1; // randomness used for the second challenge generation in double spending protection
    private ZnElement points; // number of points that the token currently stores (initially 0)
    private ZnElement z, t; // values for blinding the token group element

    /**
     * standard constructor, intializes an empty token, meaning a token storing 0 points represented as a Pedersen commitment.
     * Note that certificate is not set since a token is usually not certified upon creation.
     * @param myRemClassRing remainder class ring of exponents
     * @param sigScheme signature scheme
     * @param vKey verification key
     * @param h array of the bases for the Pedersen commitment
     * @param USK secret key of user
     * @param esk ElGamal secret key
     * @param dsrnd0 double spending randomness
     * @param dsrnd1 other double spending randomness
     */
    public Token(Zn myRemClassRing, SPSEQSignatureScheme sigScheme, VerificationKey vKey,GroupElement[] h, ZnElement USK, ZnElement esk, ZnElement dsrnd0, ZnElement dsrnd1, ZnElement z, ZnElement t) throws PedersenException
    {
        // asserting correct number of group elements passed
        if(h.length != 7)
        {
            throw new PedersenException("h is required to consist of 7 group elements, found: " + String.valueOf(h.length));
        }

        // TODO check out group element vector, vector exponentiation operation
        // initializing bases array with deep copy of passed elements array
        this.commitmentBases = new GroupElement[h.length];
        for(int i = 0; i < h.length; i++)
        {
            this.commitmentBases[i] = h[i]; // note: due to 0-based indexing, the bases' indices are off by one wrt paper
        }

        // initializing respective object variables with other parameters
        this.myRemainderClassRing = myRemClassRing;
        this.signatureScheme = sigScheme;
        this.verificationKey = vKey;
        this.userSecretKey = USK;
        this.encryptionSecretKey = esk;
        this.doubleSpendRandomness0 = dsrnd0;
        this.doubleSpendRandomness1 = dsrnd1;
        this.points = myRemainderClassRing.getZeroElement(); // initially, token contains no points
        this.z = z;
        this.t = t;

        // compute the token-representing Pedersen commitment for the first time
        this.updateToken();
    }

    /**
     * recomputes the Pedersen commitment representing the token from the group elements and exponents (and also updates its plaintext representation)
     * (see chapter 4 of 2020 incentive systems paper)
     */
    public void updateToken()
    {
        // note that indices of bases are off by one wrt paper (zero-based indexing)
        this.token = this.commitmentBases[0].pow(this.userSecretKey).op(
                this.commitmentBases[1].pow(this.encryptionSecretKey).op(
                        this.commitmentBases[2].pow(this.doubleSpendRandomness0).op(
                                this.commitmentBases[3].pow(this.doubleSpendRandomness1).op(
                                        this.commitmentBases[4].pow(this.points).op(
                                                this.commitmentBases[5].pow(this.z).op(
                                                        this.commitmentBases[6].pow(this.t)
                                                )
                                        )
                                )
                        )
                )
        );

        tokenPlainText = new GroupElementPlainText(this.token);
    }


    /**
     * sets the certificate for the token.
     * @param cert new certificate
     */
    public void setCertificate(SPSEQSignature cert) throws SPSEQException
    {
        this.certificate = cert;

        // immediately throw exception if signature not valid for token group element
        if(!signatureScheme.verify(this.tokenPlainText, this.certificate, this.verificationKey)); // token plaintext needed for verification (API reasons)
        {
            throw new SPSEQException("token was associated with an invalid certificate");
        }
    }

    /**
     * computes "dumb token" from mathematical representation of token
     * @return serialized version of token
     */
    @Override
    public Representation getRepresentation()
    {
        return ReprUtil.serialize(this);
    }

    public GroupElement getToken() { return this.token; }

    public SPSEQSignature getCertificate() { return this.certificate; }
}

