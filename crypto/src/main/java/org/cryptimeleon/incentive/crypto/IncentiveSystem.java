package org.cryptimeleon.incentive.crypto;

import lombok.Value;
import org.cryptimeleon.craco.common.ByteArrayImplementation;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
import org.cryptimeleon.incentive.crypto.model.messages.JoinRequest;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.crypto.model.proofs.CommitmentWellformednessCommonInput;
import org.cryptimeleon.incentive.crypto.model.proofs.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentive.crypto.model.proofs.CommitmentWellformednessWitness;
import org.cryptimeleon.incentive.crypto.proof.SpendDeductZkp;
import org.cryptimeleon.incentive.crypto.proof.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentive.crypto.proof.SpendDeductZkpWitnessInput;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import java.math.BigInteger;


/**
 * Contains all main algorithms of the incentive system according to 2020 incentive systems paper.
 */
@Value
public class IncentiveSystem {

    // public parameters
    public IncentivePublicParameters pp;

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

    public PromotionParameters generatePromotionParameters(int pointsVectorSize) {
        return new PromotionParameters(this.pp.getBg().getZn().getUniformlyRandomElement(), pointsVectorSize);
    }

    @Deprecated
    public PromotionParameters legacyPromotionParameters() {
        return new PromotionParameters(this.pp.getBg().getZn().getOneElement(), 1);
    }

    /*
     * implementation of the Issue {@literal <}-{@literal >}Join protocol
     */


    /**
     * functionality of the first part of the Issue algorithm of the Cryptimeleon incentive system
     *
     * @param pk  provider public key of the provider the user interacts with
     * @param ukp user key pair
     * @return join request, i.e. object representing the first two messages in the Issue-Join protocol of the Cryptimeleon incentive system
     */
    public JoinRequest generateJoinRequest(ProviderPublicKey pk, UserKeyPair ukp) {
        UserPublicKey upk = ukp.getPk();
        UserSecretKey usk = ukp.getSk();

        // generate random values needed for generation of fresh user token using PRF hashThenPRFtoZn, user secret key is hash input
        var pseudoRandVector = pp.getPrfToZn().hashThenPrfToZnVector(ukp.getSk().getPrfKey(),
                ukp.getSk(),
                6,
                "IssueJoin");
        ZnElement eskUsr = (ZnElement) pseudoRandVector.get(0);
        ZnElement dsrnd0 = (ZnElement) pseudoRandVector.get(1);
        ZnElement dsrnd1 = (ZnElement) pseudoRandVector.get(2);
        ZnElement z = (ZnElement) pseudoRandVector.get(3);
        ZnElement t = (ZnElement) pseudoRandVector.get(4);
        ZnElement u = (ZnElement) pseudoRandVector.get(5);

        var H = pk.getTokenMetadataH(this.pp);

        // compute Pedersen commitment for user token
        // need to retrieve exponent from usk object; point count of 0 is reresented by zero in used Z_n
        RingElementVector exponents = new RingElementVector(t, usk.getUsk(), eskUsr, dsrnd0, dsrnd1, z);
        GroupElement c0Pre = H.innerProduct(exponents).pow(u);
        GroupElement c1Pre = pp.getG1Generator().pow(u);

        // compute NIZKP to prove well-formedness of token
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pk));
        CommitmentWellformednessCommonInput cwfCommon = new CommitmentWellformednessCommonInput(upk.getUpk(), c0Pre, c1Pre);
        CommitmentWellformednessWitness cwfWitness = new CommitmentWellformednessWitness(usk.getUsk(), eskUsr, dsrnd0, dsrnd1, z, t, u.inv());
        FiatShamirProof cwfProof = cwfProofSystem.createProof(cwfCommon, cwfWitness);

        // assemble and return join request object (commitment, proof of well-formedness)
        return new JoinRequest(c0Pre, c1Pre, cwfProof);
    }

    /**
     * Implements the functionality of the Issue algorithm of the Cryptimeleon incentive system, i.e. handles a join request by signing the
     * included preliminary commitment after adding the provider's share for the tracking key esk.
     *
     * @param pkp key pair of the provider
     * @param upk public key of user (needed to restore upk needed to check validity of the commitment well-formedness proof)
     * @param jr  join request to be handled
     * @return join response, i.e. object representing the third message in the Issue-Join protocol
     * @throws IllegalArgumentException indicating that the proof for commitment well-formedness was rejected
     */
    public JoinResponse generateJoinRequestResponse(PromotionParameters promotionParameters, ProviderKeyPair pkp, GroupElement upk, JoinRequest jr) throws IllegalArgumentException {
        ProviderPublicKey pk = pkp.getPk();
        ProviderSecretKey sk = pkp.getSk();

        // read out parts of the precommitment and the commitment well-formedness proof from the join request object
        GroupElement c0Pre = jr.getPreCommitment0();
        GroupElement c1Pre = jr.getPreCommitment1();
        FiatShamirProof cwfProof = jr.getCwfProof();

        // reassemble common input for the commitment well-formedness proof
        CommitmentWellformednessCommonInput cwfProofCommonInput = new CommitmentWellformednessCommonInput(upk, c0Pre, c1Pre);

        // check commitment well-formedness proof for validity
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pk));
        if (!cwfProofSystem.checkProof(cwfProofCommonInput, cwfProof)) {
            throw new IllegalArgumentException("The proof of the commitment being well-formed was rejected.");
        }

        // modify precommitment 0 using homomorphism trick and randomly chosen exponent
        ZnElement eskProv = pp.getBg().getZn().getUniformlyRandomElement();
        GroupElement modifiedC0Pre = c0Pre.op(c1Pre.pow(sk.getQ().get(1).mul(eskProv)));

        // create certificate for modified pre-commitment vector
        SPSEQSignature cert = (SPSEQSignature) pp.getSpsEq().sign(sk.getSkSpsEq(),
                modifiedC0Pre,
                c1Pre,
                c1Pre.pow(promotionParameters.getPromotionId())); // first argument: signing keys, other arguments form the msg vector

        // assemble and return join response object
        return new JoinResponse(cert, eskProv);
    }

    /**
     * Implements the second part of the functionality of the Issue algorithm from the Cryptimeleon incentive system, i.e. computes the final user data
     * (token and corresponding certificate) from the signed preliminary token from the passed join request and response.
     *
     * @param pk   public key of the provider the user interacted with
     * @param ukp  key pair of the user handling the response
     * @param jReq the initial join request of the user handling the response to it
     * @param jRes join response to be handled
     * @return token containing 0 points
     */
    public Token handleJoinRequestResponse(PromotionParameters promotionParameters, ProviderPublicKey pk, UserKeyPair ukp, JoinRequest jReq, JoinResponse jRes) {
        // re-generate random values from join request generation of fresh user token using PRF hashThenPRFtoZn, user secret key is hash input
        var pseudoRandVector = pp.getPrfToZn().hashThenPrfToZnVector(ukp.getSk().getPrfKey(), ukp.getSk(), 6, "IssueJoin");
        ZnElement eskUsr = (ZnElement) pseudoRandVector.get(0);
        ZnElement dsrnd0 = (ZnElement) pseudoRandVector.get(1);
        ZnElement dsrnd1 = (ZnElement) pseudoRandVector.get(2);
        ZnElement z = (ZnElement) pseudoRandVector.get(3);
        ZnElement t = (ZnElement) pseudoRandVector.get(4);
        ZnElement u = (ZnElement) pseudoRandVector.get(5);

        // extract relevant variables from join request, join response and public parameters
        GroupElement c0Pre = jReq.getPreCommitment0();
        SPSEQSignature preCert = jRes.getPreCertificate();
        ZnElement eskProv = jRes.getEskProv();
        SPSEQSignatureScheme usedSpsEq = pp.getSpsEq();

        // re-compute modified pre-commitment for token
        GroupElement h2 = pk.getH().get(1);
        GroupElement modifiedC0Pre = c0Pre.op(h2.pow(u.mul(eskProv)));

        // verify the signature on the modified pre-commitment
        if (!usedSpsEq.verify(pk.getPkSpsEq(),
                preCert,
                modifiedC0Pre,
                jReq.getPreCommitment1(),
                jReq.getPreCommitment1().pow(promotionParameters.getPromotionId()))) {
            throw new RuntimeException("signature on pre-commitment's left part is not valid!");
        }

        // change representation of token-certificate pair
        SPSEQSignature finalCert = (SPSEQSignature) usedSpsEq.chgRep(preCert, u.inv(), pk.getPkSpsEq()); // adapt signature
        GroupElement finalCommitment0 = modifiedC0Pre.pow(u.inv()); // need to adapt message manually (entry by entry), used equivalence relation is R_exp
        GroupElement finalCommitment1 = pp.getG1Generator();

        // assemble and return token
        ZnElement esk = eskUsr.add(eskProv);
        Zn usedZn = pp.getBg().getZn();
        RingElementVector zeros = RingElementVector.generate(usedZn::getZeroElement, promotionParameters.getPointsVectorSize());

        return new Token(finalCommitment0, finalCommitment1, esk, dsrnd0, dsrnd1, z, t, promotionParameters.getPromotionId(), zeros, finalCert);
    }

    /*
     * end of the implementation of the Issue {@literal <}-{@literal >}Join protocol
     */


    /**
     * implementation of the Credit {@literal <}-{@literal >}Earn protocol
     * <p>
     * Generate an earn request that blinds the token and signature such that the provider can compute a signature on
     * a matching token with added value.
     *
     * @param token             the token to update
     * @param providerPublicKey the public key of the provider
     * @param userKeyPair       the key pair of the user submitting the request
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
                token.getCommitment0().pow(s).compute(),  // Compute for concurrent computation
                token.getCommitment1().pow(s).compute()
        );
    }

    /**
     * Generate the response for users' earn requests to update the blinded token.
     * Computes a valid signature on a blinded token matching the received token, but with k more points.
     *
     * @param earnRequest     the earn request to process. It can be assumed, that all group elements of earnRequest
     *                        are already computed since they are usually directly parsed from a representation
     * @param providerKeyPair the provider key pair
     * @return a signature on a blinded, updated token
     */
    public SPSEQSignature generateEarnRequestResponse(PromotionParameters promotionParameters, EarnRequest earnRequest, Vector<BigInteger> deltaK, ProviderKeyPair providerKeyPair) {
        var C0 = earnRequest.getC0();
        var C1 = earnRequest.getC1();

        // Verify the blinded signature for the blinded commitment is valid
        var isSignatureValid = pp.getSpsEq().verify(
                providerKeyPair.getPk().getPkSpsEq(),
                earnRequest.getBlindedSignature(),
                C0,
                C1,
                C1.pow(promotionParameters.getPromotionId())
        );
        if (!isSignatureValid) {
            throw new IllegalArgumentException("Signature is not valid");
        }

        // Sign a blinded commitment with k more points

        var Q = providerKeyPair.getSk().getTokenPointsQ(promotionParameters);
        var K = deltaK.map(k -> pp.getBg().getG1().getZn().createZnElement(k));

        return (SPSEQSignature) pp.getSpsEq().sign(
                providerKeyPair.getSk().getSkSpsEq(),
                C0.op(C1.pow(Q.innerProduct(K))).compute(), // Add k blinded point to the commitment
                C1,
                C1.pow(promotionParameters.getPromotionId())
        );
    }

    /**
     * @param earnRequest       the earn request that was originally sent. Again, it can be assumed that all elements
     *                          are computed since the earnRequest usually is serialized to a representation before.
     * @param changedSignature  the signature computed by the provider
     * @param deltaK            the increase for the users token value
     * @param token             the old token
     * @param providerPublicKey public key of the provider
     * @param userKeyPair       keypair of the user
     * @return new token with value of the old token + k
     */
    public Token handleEarnRequestResponse(PromotionParameters promotionParameters,
                                           EarnRequest earnRequest,
                                           SPSEQSignature changedSignature,
                                           Vector<BigInteger> deltaK,
                                           Token token,
                                           ProviderPublicKey providerPublicKey,
                                           UserKeyPair userKeyPair) {

        // Pseudorandom randomness s used for blinding in the request
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn-s");
        var K = RingElementVector.fromStream(deltaK.stream().map(e -> pp.getBg().getZn().createZnElement(e)));

        // Recover blinded commitments (to match the commitments signed by the prover) with updated value
        var blindedNewC0 = earnRequest.getC0()
                .op(providerPublicKey.getTokenPointsH(promotionParameters)
                        .pow(s)
                        .innerProduct(K)
                ).compute();
        var blindedNewC1 = earnRequest.getC1();

        // Verify signature on recovered commitments
        var signatureValid = pp.getSpsEq().verify(providerPublicKey.getPkSpsEq(),
                changedSignature,
                blindedNewC0,
                blindedNewC1,
                blindedNewC1.pow(promotionParameters.getPromotionId()));
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
                token.getPromotionId(),
                new RingElementVector(token.getPoints().zip(
                        K,
                        RingElement::add
                )),
                (SPSEQSignature) newSignature
        );
    }

    /*
     * end of the implementation of the Credit {@literal <}-{@literal >}Earn protocol
     */


    /**
     * implementation of the Deduct {@literal <}-{@literal >}Spend protocol
     * <p>
     * Generates a request to add value k to token.
     *
     * @param token             the token
     * @param providerPublicKey public key of the provider
     * @param deltaK            amount to subtract from the token's points vector
     * @param userKeyPair       keypair of the user that owns the token
     * @param tid               transaction ID, provided by the provider
     * @return serializable spendRequest that can be sent to the provider
     */
    public SpendRequest generateSpendRequest(PromotionParameters promotionParameters,
                                             Token token,
                                             ProviderPublicKey providerPublicKey,
                                             Vector<BigInteger> deltaK, // todo think about this api, we could also pass in the new values instead of changes
                                             UserKeyPair userKeyPair,
                                             Zn.ZnElement tid
    ) {
        // Some local variables and pre-computations to make the code more readable
        var zp = pp.getBg().getZn();
        var usk = userKeyPair.getSk().getUsk();
        var esk = token.getEncryptionSecretKey();
        var dsid = pp.getW().pow(esk);
        var vectorH = providerPublicKey.getH(this.pp, promotionParameters);
        var vectorR = zp.getUniformlyRandomElements(pp.getNumEskDigits());
        var K = RingElementVector.fromStream(deltaK.stream().map(e -> pp.getBg().getZn().createZnElement(e)));


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
        var exponents = new RingElementVector(tS, usk, eskUsrS, dsrnd0S, dsrnd1S, zS).concatenate(token.getPoints().zip(K, RingElement::sub));
        var cPre0 = vectorH.innerProduct(exponents).pow(uS).compute();
        var cPre1 = pp.getG1Generator().pow(uS).compute();

        /* Enable double-spending-protection by forcing usk and esk becoming public in that case
           If token is used twice in two different transactions, the provider observes (c0,c1), (c0',c1') with gamma!=gamma'
           Hence, the provider can easily retrieve usk and esk (using the Schnorr-trick, computing (c0-c0')/(gamma-gamma') for usk, analogously for esk). */
        var gamma = Util.hashGamma(zp, K, dsid, tid, cPre0, cPre1);
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
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerPublicKey, promotionParameters));
        var witness = new SpendDeductZkpWitnessInput(usk, token.getZ(), zS, token.getT(), tS, uS, esk, eskUsrS, token.getDoubleSpendRandomness0(), dsrnd0S, token.getDoubleSpendRandomness1(), dsrnd1S, eskUsrSDec, vectorR, token.getPoints());
        var commonInput = new SpendDeductZkpCommonInput(gamma, c0, c1, dsid, cPre0, cPre1, token.getCommitment0(), cTrace0, cTrace1, K);
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        // Assemble request
        return new SpendRequest(dsid, proof, c0, c1, cPre0, cPre1, cTrace0, cTrace1, token.getCommitment0(), token.getSignature());
    }

    /**
     * React to a legitimate spend request to allow the user retrieving an updated token with the value decreased by k.
     * Returns additional data for double-spending protection.
     *
     * @param spendRequest    the user's request
     * @param providerKeyPair keypair of the provider
     * @param deltaK          the amount to subtract from the user's token
     * @param tid             transaction id, should be verified by the provider
     * @return tuple of response to send to the user and information required for double-spending protection
     */
    public SpendProviderOutput generateSpendRequestResponse(PromotionParameters promotionParameters,
                                                            SpendRequest spendRequest,
                                                            ProviderKeyPair providerKeyPair,
                                                            Vector<BigInteger> deltaK,
                                                            Zn.ZnElement tid) {

        var K = RingElementVector.fromStream(deltaK.stream().map(e -> pp.getBg().getZn().createZnElement(e)));

        /* Verify that the request is valid and well-formed */

        // Verify signature of the old token (C1 must be g1 according to ZKP in T2 paper. We omit the ZKP and use g1 instead of C1)
        var signatureValid = pp.getSpsEq().verify(providerKeyPair.getPk().getPkSpsEq(),
                spendRequest.getSigma(),
                spendRequest.getCommitmentC0(),
                pp.getG1Generator(),
                pp.getG1Generator().pow(promotionParameters.getPromotionId()));
        if (!signatureValid) {
            throw new IllegalArgumentException("Signature of the request is not valid!");
        }

        // Validate ZKP
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerKeyPair.getPk(), promotionParameters));
        var gamma = Util.hashGamma(pp.getBg().getZn(), K, spendRequest.getDsid(), tid, spendRequest.getCPre0(), spendRequest.getCPre1());
        var commonInput = new SpendDeductZkpCommonInput(spendRequest, K, gamma);
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
                cPre1,
                cPre1.pow(promotionParameters.getPromotionId())
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
     * @param deltaK            amount to subtract from the token
     * @param userKeyPair       keypair of the user
     * @return token with the value of the old token + k
     */
    public Token handleSpendRequestResponse(PromotionParameters promotionParameters,
                                            SpendResponse spendResponse,
                                            SpendRequest spendRequest,
                                            Token token,
                                            Vector<BigInteger> deltaK,
                                            ProviderPublicKey providerPublicKey,
                                            UserKeyPair userKeyPair) {

        var K = RingElementVector.fromStream(deltaK.stream().map(e -> pp.getBg().getZn().createZnElement(e)));

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
        var blindedCStar1 = pp.getG1Generator().pow(uS); // Recompute, just to make sure
        var valid = pp.getSpsEq().verify(providerPublicKey.getPkSpsEq(),
                spendResponse.getSigma(),
                blindedCStar0,
                blindedCStar1,
                blindedCStar1.pow(promotionParameters.getPromotionId()));
        if (!valid) {
            throw new IllegalArgumentException("Signature is not valid");
        }

        // Build new token
        return new Token(
                blindedCStar0.pow(uS.inv()), // Unblind commitment
                pp.getG1Generator(), // Same as unblinded CStar1
                eskUsrS.add(spendResponse.getEskProvStar()), // esk^* is sum of user's and providers new esk
                dsrnd0S,
                dsrnd1S,
                zS,
                tS,
                token.getPromotionId(),
                new RingElementVector(token.getPoints().zip(
                        K,
                        RingElement::sub
                )),
                // Change representation of signature to match the un-blinded commitments
                (SPSEQSignature) pp.getSpsEq().chgRep(spendResponse.getSigma(), uS.inv(), providerPublicKey.getPkSpsEq())
        );
    }

    /*
     * end of the implementation of the Deduct {@literal <}-{@literal >}Spend protocol
     */


    /*
     * methods for offline double-spending detection
     */

    void link() {
    }

    void verifyDS() {
    }

    void trace() {
    }

    /*
     * end of methods for offline double-spending detection
     */
}
