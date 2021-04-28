package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.common.ByteArrayImplementation;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.*;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkp;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkpWitnessInput;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;


/*
 * Contains all main algorithms of the incentive system.
 */
public class IncentiveSystem {

    // Public parameters
    public final IncentivePublicParameters pp;

    public IncentiveSystem(IncentivePublicParameters pp) {
        this.pp = pp;
    }

    /**
     * Generate public parameters for the incentive system.
     *
     * @param securityParameter   the security parameter used in the setup algorithm
     * @param bilinearGroupChoice the bilinear group to use. Especially useful for testing
     * @return public parameters for the incentive system
     */
    public static IncentivePublicParameters setup(int securityParameter, Setup.BilinearGroupChoice bilinearGroupChoice) {
        return Setup.trustedSetup(securityParameter, bilinearGroupChoice);
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

    /**
     * Generate an earn request that blinds the token and signature such that the provider can compute a signature on
     * a matching token with added value.
     *
     * @param token             the token to update
     * @param providerPublicKey the public key of the provider
     * @return request to give to a provider
     */
    public EarnRequest generateEarnRequest(Token token, ProviderPublicKey providerPublicKey, UserKeyPair userKeyPair) {
        // Compute pseudorandom value from the token that is used to blind the commitment
        // This makes this algorithm deterministic
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn-s");

        // Blind commitments and change representation of signature such that it is valid for blinded commitments
        // The blinded commitments and signature are sent to the provider
        return new EarnRequest(
                (SPSEQSignature) pp.getSpsEq().chgRep(
                        token.getSignature(),
                        s,
                        providerPublicKey.getPkSpsEq()
                ),
                token.getC0().pow(s).compute(),  // Compute for concurrent computation
                token.getC1().pow(s).compute()
        );
    }

    /**
     * Generate the response for users' earn requests to update the blinded token.
     * Computes a valid signature on a blinded token matching the received token, but with k more points.
     *
     * @param earnRequest     the earn request to process. It can be assumed, that all group elements of earnRequest
     *                        are already computed since they are usually directly parsed from a representation
     * @param k               the increase for the users token value
     * @param providerKeyPair the provider key pair
     * @return a signature on a blinded, updated token
     */
    public SPSEQSignature generateEarnRequestResponse(EarnRequest earnRequest, BigInteger k, ProviderKeyPair providerKeyPair) {

        // Verify the blinded signature for the blinded commitment is valid
        var isSignatureValid = pp.getSpsEq().verify(providerKeyPair.getPk().getPkSpsEq(), earnRequest.getBlindedSignature(), earnRequest.getC0(), earnRequest.getC1());
        if (!isSignatureValid) {
            throw new IllegalArgumentException("Signature is not valid");
        }

        // Sign a blinded commitment with k more points
        var C0 = earnRequest.getC0();
        var C1 = earnRequest.getC1();
        var q4 = providerKeyPair.getSk().getQ().get(4);

        return (SPSEQSignature) pp.getSpsEq().sign(
                providerKeyPair.getSk().getSkSpsEq(),
                C0.op(C1.pow(q4.mul(k))).compute(), // Add k blinded point to the commitment
                C1
        );
    }

    /**
     * @param earnRequest       the earn request that was originally sent. Again, it can be assumed that all elements
     *                          are computed since the earnRequest usually is serialized to a representation before.
     * @param changedSignature  the signature computed by the provider
     * @param k                 the increase for the users token value
     * @param token             the old token
     * @param providerPublicKey public key of the provider
     * @param userKeyPair       keypair of the user
     * @return new token with value of the old token + k
     */
    public Token handleEarnRequestResponse(EarnRequest earnRequest, SPSEQSignature changedSignature, BigInteger k, Token token, ProviderPublicKey providerPublicKey, UserKeyPair userKeyPair) {

        // Pseudorandom randomness s used for blinding in the request
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn-s");

        // Recover blinded commitments (to match the commitments signed by the prover) with updated value
        var blindedNewC0 = earnRequest.getC0().op((providerPublicKey.getH().get(4).pow(s)).pow(k)).compute();
        var blindedNewC1 = earnRequest.getC1();

        // Verify signature on recovered commitments
        var signatureValid = pp.getSpsEq().verify(providerPublicKey.getPkSpsEq(), changedSignature, blindedNewC0, blindedNewC1);
        if (!signatureValid) {
            throw new IllegalArgumentException("Signature is not valid");
        }

        // Change representation of signature such that it is valid for un-blinded commitment
        var newSignature = pp.getSpsEq().chgRep(changedSignature, s.inv(), providerPublicKey.getPkSpsEq());

        // Assemble new token
        return new Token(
                blindedNewC0.pow(s.inv()).compute(), // Un-blind commitment
                blindedNewC1.pow(s.inv()).compute(), // see above
                token.getEncryptionSecretKey(),
                token.getDoubleSpendRandomness0(),
                token.getDoubleSpendRandomness1(),
                token.getZ(),
                token.getT(),
                token.getPoints().add(pp.getBg().getZn().valueOf(k)),
                (SPSEQSignature) newSignature
        );
    }

    /**
     * Generates a request to add value k to token.
     *
     * @param token             the token
     * @param providerPublicKey public key of the provider
     * @param k                 amount to add to the token's value
     * @param userKeyPair       keypair of the user that owns the token
     * @param tid               transaction ID, provided by the provider
     * @return serializable spendRequest that can be sent to the provider
     */
    public SpendRequest generateSpendRequest(Token token,
                                             ProviderPublicKey providerPublicKey,
                                             BigInteger k,
                                             UserKeyPair userKeyPair,
                                             Zn.ZnElement tid
    ) {
        // Some local variables and pre-computations to make the code more readable
        var zp = pp.getBg().getZn();
        var usk = userKeyPair.getSk().getUsk();
        var esk = token.getEncryptionSecretKey();
        var dsid = pp.getW().pow(esk);
        var vectorH = providerPublicKey.getH().append(pp.getH7());
        var vectorR = zp.getUniformlyRandomElements(pp.getNumEskDigits());


        /* Compute pseudorandom values */
        // As in credit-earn, we use the PRF to make the algorithm deterministic
        var prfZnElements = pp.getPrfToZn().hashThenPrfToZnVector(userKeyPair.getSk().getPrfKey(), token, 6, "SpendDeduct");
        Zn.ZnElement eskUsrS = (Zn.ZnElement) prfZnElements.get(0);
        Zn.ZnElement dsrnd0S = (Zn.ZnElement) prfZnElements.get(1);
        Zn.ZnElement dsrnd1S = (Zn.ZnElement) prfZnElements.get(2);
        Zn.ZnElement zS = (Zn.ZnElement) prfZnElements.get(3);
        Zn.ZnElement tS = (Zn.ZnElement) prfZnElements.get(4);
        Zn.ZnElement uS = (Zn.ZnElement) prfZnElements.get(5);

        // Prepare a new commitment (cPre0, cPre1) based on the pseudorandom values
        var exponents = new RingElementVector(usk, eskUsrS, dsrnd0S, dsrnd1S, token.getPoints().sub(zp.valueOf(k)), zS, tS);
        var cPre0 = vectorH.innerProduct(exponents).pow(uS).compute();
        var cPre1 = pp.getG1().pow(uS).compute();

        /* Enable double-spending-protection by forcing usk and esk becoming public in that case
           If token is used twice in two different transactions, the provider observes (c0,c1), (c0',c1') with gamma!=gamma'
           Hence, the provider can easily retrieve usk and esk. */
        var gamma = Util.hashGamma(zp, k, dsid, tid, cPre0, cPre1);
        var c0 = usk.mul(gamma).add(token.getDoubleSpendRandomness0());
        var c1 = esk.mul(gamma).add(token.getDoubleSpendRandomness1());

        /* Compute El-Gamal encryption of esk^*_usr using under secret key esk
           This allows the provider to decrypt usk^*_usr in case of double spending with the leaked esk.
           By additionally storing esk^*_prov, the provider can retrieve esk^* and thus iteratively decrypt the new esks. */

        // Decompose the encryption-secret-key to base eskDecBase and map the digits to Zn
        var eskUsrSDecBigInt = IntegerRing.decomposeIntoDigits(eskUsrS.asInteger(), pp.getEskDecBase().asInteger(), pp.getNumEskDigits());
        var eskUsrSDec = RingElementVector.generate(i -> zp.valueOf(eskUsrSDecBigInt[i]), eskUsrSDecBigInt.length);

        // Encrypt digits using El-Gamal and the randomness r
        var cTrace0 = pp.getW().pow(vectorR).compute();
        var cTrace1 = cTrace0.pow(esk).op(pp.getW().pow(eskUsrSDec)).compute();

        /* Build noninteractive (Fiat-Shamir transformed) ZKP to ensure that the user follows the rules of the protocol */
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerPublicKey));
        var witness = new SpendDeductZkpWitnessInput(usk, token.getPoints(), token.getZ(), zS, token.getT(), tS, uS, esk, eskUsrS, token.getDoubleSpendRandomness0(), dsrnd0S, token.getDoubleSpendRandomness1(), dsrnd1S, eskUsrSDec, vectorR);
        var commonInput = new SpendDeductZkpCommonInput(k, gamma, c0, c1, dsid, cPre0, cPre1, token.getC0(), cTrace0, cTrace1);
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        // Assemble request
        return new SpendRequest(dsid, proof, c0, c1, cPre0, cPre1, cTrace0, cTrace1, token.getC0(), token.getSignature());
    }

    /**
     * React to a legitimate spend request to allow the user retrieving an updated token with the value increased by k.
     * Returns additional data for double-spending protection.
     *
     * @param spendRequest    the user's request
     * @param providerKeyPair keypair of the provider
     * @param k               the amount to add to the user's token
     * @param tid             transaction id, should be verified by the provider
     * @return tuple of response to send to the user and information required for double-spending protection
     */
    public SpendProviderOutput generateSpendRequestResponse(SpendRequest spendRequest, ProviderKeyPair providerKeyPair, BigInteger k, Zn.ZnElement tid) {
        /* Verify that the request is valid and well-formed */

        // Verify signature of the old token (C1 must be g1 according to ZKP in T2 paper. We omit the ZKP and use g1 instead of C1)
        var signatureValid = pp.getSpsEq().verify(providerKeyPair.getPk().getPkSpsEq(), spendRequest.getSigma(), spendRequest.getCommitmentC0(), pp.getG1());
        if (!signatureValid) {
            throw new IllegalArgumentException("Signature of the request is not valid!");
        }

        // Validate ZKP
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerKeyPair.getPk()));
        var gamma = Util.hashGamma(pp.getBg().getZn(), k, spendRequest.getDsid(), tid, spendRequest.getCPre0(), spendRequest.getCPre1());
        var commonInput = new SpendDeductZkpCommonInput(spendRequest, k, gamma);
        var proofValid = fiatShamirProofSystem.checkProof(commonInput, spendRequest.getSpendDeductZkp());
        if (!proofValid) {
            throw new IllegalArgumentException("ZPK of the request is not valid!");
        }

        /* Request is valid. Compute new blinded token and signature */
        // Retrieve esk^*_prov via PRF
        var preimage = new ByteArrayAccumulator();
        preimage.escapeAndSeparate(commonInput.c0Pre);
        preimage.escapeAndSeparate(commonInput.c1Pre);
        var eskStarProv = pp.getPrfToZn().hashThenPrfToZn(providerKeyPair.getSk().getBetaProv(), new ByteArrayImplementation(preimage.extractBytes()), "eskStarProv");

        // Compute blind signature on new, still blinded commitment
        var cPre0 = spendRequest.getCPre0();
        var cPre1 = spendRequest.getCPre1();
        var sigmaPrime = (SPSEQSignature) pp.getSpsEq().sign(
                providerKeyPair.getSk().getSkSpsEq(),
                cPre0.op(cPre1.pow(eskStarProv.mul(providerKeyPair.getSk().getQ().get(1)))),
                cPre1
        );

        // Assemble providers and users output and return as a tuple
        return new SpendProviderOutput(
                new SpendResponse(sigmaPrime, eskStarProv),
                new DoubleSpendingTag(commonInput.c0, commonInput.c1, gamma, eskStarProv, commonInput.ctrace0, commonInput.ctrace1)
        );


    }

    /**
     * Process the response to a spendRequest and compute the updated token.
     *
     * @param spendResponse     the provider's response
     * @param spendRequest      the original request
     * @param token             the old token
     * @param providerPublicKey public key of the provider
     * @param k                 amount to add to the token
     * @param userKeyPair       keypair of the user
     * @return token with the value of the old token + k
     */
    public Token handleSpendRequestResponse(SpendResponse spendResponse,
                                            SpendRequest spendRequest,
                                            Token token,
                                            BigInteger k,
                                            ProviderPublicKey providerPublicKey,
                                            UserKeyPair userKeyPair) {
        // Re-compute pseudorandom values
        var prfZnElements = pp.getPrfToZn().hashThenPrfToZnVector(userKeyPair.getSk().getPrfKey(), token, 6, "SpendDeduct");
        Zn.ZnElement eskUsrS = (Zn.ZnElement) prfZnElements.get(0);
        Zn.ZnElement dsrnd0S = (Zn.ZnElement) prfZnElements.get(1);
        Zn.ZnElement dsrnd1S = (Zn.ZnElement) prfZnElements.get(2);
        Zn.ZnElement zS = (Zn.ZnElement) prfZnElements.get(3);
        Zn.ZnElement tS = (Zn.ZnElement) prfZnElements.get(4);
        Zn.ZnElement uS = (Zn.ZnElement) prfZnElements.get(5);

        // Verify the signature on the new, blinded commitment
        var blindedCStar0 = spendRequest.getCPre0().op(providerPublicKey.getH().get(1).pow(spendResponse.getEskProvStar().mul(uS)));
        var blindedCStar1 = pp.getG1().pow(uS); // Recompute, just to make sure
        var valid = pp.getSpsEq().verify(providerPublicKey.getPkSpsEq(), spendResponse.getSigma(), blindedCStar0, blindedCStar1);
        if (!valid) {
            throw new IllegalArgumentException("Signature is not valid");
        }

        // Build new token
        return new Token(
                blindedCStar0.pow(uS.inv()), // Unblind commitment
                pp.getG1(), // Same as unblinded CStar1
                eskUsrS.add(spendResponse.getEskProvStar()), // esk^* is sum of user's and providers new esk
                dsrnd0S,
                dsrnd1S,
                zS,
                tS,
                token.getPoints().sub(pp.getBg().getZn().valueOf(k)),
                // Change representation of signature to match the un-blinded commitments
                (SPSEQSignature) pp.getSpsEq().chgRep(spendResponse.getSigma(), uS.inv(), providerPublicKey.getPkSpsEq())
        );
    }

    void link() {
    }

    void verifyDS() {
    }

    void trace() {
    }
}
