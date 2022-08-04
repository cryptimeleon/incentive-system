package org.cryptimeleon.incentive.crypto;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.common.ByteArrayImplementation;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
import org.cryptimeleon.incentive.crypto.model.messages.JoinRequest;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpWitnessInput;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessCommonInput;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessWitness;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


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
     * Generate public parameters for the incentive system. Wrapper for the trustedSetup method of the Setup class.
     *
     * @param securityParameter   the security parameter used in the setup algorithm
     * @param bilinearGroupChoice the bilinear group to use. Especially useful for testing
     * @return public parameters for the incentive system
     */
    public static IncentivePublicParameters setup(int securityParameter, Setup.BilinearGroupChoice bilinearGroupChoice) {
        return Setup.trustedSetup(securityParameter, bilinearGroupChoice);
    }

    public static PromotionParameters generatePromotionParameters(int pointsVectorSize) {
        return new PromotionParameters(BigInteger.valueOf(RandomGenerator.getRandomNumber(Long.MIN_VALUE, Long.MAX_VALUE)), pointsVectorSize);
    }

    /**
     * wrapper for the provider key generation method in Setup
     *
     * @return fresh provider key pair
     */
    public ProviderKeyPair generateProviderKeys() {
        return Setup.providerKeyGen(this.pp);
    }

    /**
     * wrapper for the user key generation method from Setup
     *
     * @return fresh user key pair
     */
    public UserPreKeyPair generateUserKeys() {
        return Setup.userKeyGen(this.pp);
    }

    @Deprecated
    public PromotionParameters legacyPromotionParameters() {
        return new PromotionParameters(BigInteger.ONE, 1);
    }


    /**
     * Sign a verified (userPublicKey, w) tuple for the genesis process:
     * We can force users to such a signed public key in all their tokens.
     */
   public SPSEQSignature signVerifiedUserPublicKey(ProviderKeyPair providerKeyPair, UserPublicKey userPublicKey) {
       return (SPSEQSignature) pp.getSpsEq().sign(
               providerKeyPair.getSk().getGenesisSpsEqSk(),
               userPublicKey.getUpk(),
               pp.getW()
       );
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
    public JoinRequest generateJoinRequest(ProviderPublicKey pk, UserKeyPair ukp, PromotionParameters promotionParameters) {
        UserPublicKey upk = ukp.getPk();
        UserSecretKey usk = ukp.getSk();



        // generate random values needed for generation of fresh user token using PRF hashThenPRFtoZn, user secret key is hash input
        IssueJoinRandomness R = computeIssueJoinRandomness(ukp.getSk(), promotionParameters);

        // blind genesis signature
        GroupElement blindedUpk = upk.getUpk().pow(R.blindGenesisR);
        GroupElement blindedW = pp.getW().pow(R.blindGenesisR);
        SPSEQSignature blindedGenesisSignature = (SPSEQSignature) pp.getSpsEq().chgRep(
                        ukp.getSk().getGenesisSignature(),
                        R.blindGenesisR,
                        pk.getGenesisSpsEqPk()
                );
        assert pp.getSpsEq().verify(pk.getGenesisSpsEqPk(), blindedGenesisSignature, blindedUpk, blindedW);


        GroupElementVector H = pk.getTokenMetadataH(this.pp);

        // compute Pedersen commitment for user token
        // need to retrieve exponent from usk object; point count of 0 is reresented by zero in used Z_n
        RingElementVector exponents = new RingElementVector(R.t, usk.getUsk(), R.eskUsr, R.dsrnd0, R.dsrnd1, R.z);
        GroupElement c0Pre = H.innerProduct(exponents).pow(R.u);
        GroupElement c1Pre = pp.getG1Generator().pow(R.u);

        // compute NIZKP to prove well-formedness of token
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pk));
        CommitmentWellformednessCommonInput cwfCommon = new CommitmentWellformednessCommonInput(c0Pre, c1Pre, blindedUpk, blindedW);
        CommitmentWellformednessWitness cwfWitness = new CommitmentWellformednessWitness(usk.getUsk(), R.eskUsr, R.dsrnd0, R.dsrnd1, R.z, R.t, R.u.inv());
        FiatShamirProof cwfProof = cwfProofSystem.createProof(cwfCommon, cwfWitness);

        // assemble and return join request object (commitment, proof of well-formedness)
        return new JoinRequest(c0Pre, c1Pre, cwfProof, blindedUpk, blindedW, blindedGenesisSignature);
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

        // read out parts of the pre-commitment and the commitment well-formedness proof from the join request object
        GroupElement c0Pre = jr.getPreCommitment0();
        GroupElement c1Pre = jr.getPreCommitment1();
        GroupElement blindedW = jr.getBlindedW();
        GroupElement blindedUpk = jr.getBlindedUpk();
        FiatShamirProof cwfProof = jr.getCwfProof();

        // Verify genesis signature
        SPSEQSignature blindedGenesisSignature = jr.getBlindedGenesisSignature();
        if (!pp.getSpsEq().verify(pk.getGenesisSpsEqPk(), blindedGenesisSignature, blindedUpk, blindedW)) {
            throw new IllegalArgumentException("The blinded genesis signature is invalid!");
        }

        // reassemble common input for the commitment well-formedness proof
        CommitmentWellformednessCommonInput cwfProofCommonInput = new CommitmentWellformednessCommonInput(c0Pre, c1Pre, blindedUpk, blindedW);

        // check commitment well-formedness proof for validity
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pk));
        if (!cwfProofSystem.checkProof(cwfProofCommonInput, cwfProof)) {
            throw new IllegalArgumentException("The proof of the commitment being well-formed was rejected.");
        }

        // modify pre-commitment 0 using homomorphism trick and randomly chosen exponent
        ZnElement eskProv = pp.getBg().getZn().getUniformlyRandomElement();
        GroupElement modifiedC0Pre = c0Pre.op(c1Pre.pow(sk.getQ().get(1).mul(eskProv)));

        // create certificate for modified pre-commitment vector
        SPSEQSignature cert = (SPSEQSignature) pp.getSpsEq().sign(
                sk.getSkSpsEq(),
                modifiedC0Pre,
                c1Pre,
                c1Pre.pow(promotionParameters.getPromotionId())
        ); // first argument: signing keys, other arguments form the msg vector

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
        IssueJoinRandomness R = computeIssueJoinRandomness(ukp.getSk(), promotionParameters);

        // extract relevant variables from join request, join response and public parameters
        GroupElement c0Pre = jReq.getPreCommitment0();
        SPSEQSignature preCert = jRes.getPreCertificate();
        ZnElement eskProv = jRes.getEskProv();
        SPSEQSignatureScheme usedSpsEq = pp.getSpsEq();

        // re-compute modified pre-commitment for token
        GroupElement h2 = pk.getH().get(1);
        GroupElement modifiedC0Pre = c0Pre.op(h2.pow(R.u.mul(eskProv)));

        // verify the signature on the modified pre-commitment
        if (!usedSpsEq.verify(
                pk.getPkSpsEq(),
                preCert,
                modifiedC0Pre,
                jReq.getPreCommitment1(),
                jReq.getPreCommitment1().pow(promotionParameters.getPromotionId())
        )) {
            throw new RuntimeException("signature on pre-commitment's left part is not valid!");
        }

        // change representation of token-certificate pair
        SPSEQSignature finalCert = (SPSEQSignature) usedSpsEq.chgRep(preCert, R.u.inv(), pk.getPkSpsEq()); // adapt signature
        GroupElement finalCommitment0 = modifiedC0Pre.pow(R.u.inv()); // need to adapt message manually (entry by entry), used equivalence relation is R_exp
        GroupElement finalCommitment1 = pp.getG1Generator();

        // assemble and return token
        ZnElement esk = R.eskUsr.add(eskProv);
        Zn usedZn = pp.getBg().getZn();
        RingElementVector zeros = RingElementVector.generate(usedZn::getZeroElement, promotionParameters.getPointsVectorSize());

        return new Token(finalCommitment0, finalCommitment1, esk, R.dsrnd0, R.dsrnd1, R.z, R.t, promotionParameters.getPromotionId(), zeros, finalCert);
    }

    /*
     * end of the implementation of the Issue {@literal <}-{@literal >}Join protocol
     */


    /*
     * implementation of the Credit {@literal <}-{@literal >}Earn protocol
     */


    /**
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
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn");

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
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn");
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


    /*
     * implementation of the Deduct {@literal <}-{@literal >}Spend protocol
     */


    /**
     * Generates a request to add value k to token.
     *
     * @param promotionParameters the current promotion's parameters
     * @param token               the token
     * @param providerPublicKey   public key of the provider
     * @param newPoints           the new points vector the token should have
     * @param userKeyPair         keypair of the user that owns the token
     * @param tid                 transaction ID, provided by the provider
     * @param spendDeductTree     the zero knowledge proof for this promotion
     * @return serializable spendRequest that can be sent to the provider
     */
    public SpendRequest generateSpendRequest(PromotionParameters promotionParameters,
                                             Token token,
                                             ProviderPublicKey providerPublicKey,
                                             Vector<BigInteger> newPoints,
                                             UserKeyPair userKeyPair,
                                             ZnElement tid,
                                             SpendDeductTree spendDeductTree
    ) {
        // Some local variables and pre-computations to make the code more readable
        var zp = pp.getBg().getZn();
        var usk = userKeyPair.getSk().getUsk();
        var esk = token.getEncryptionSecretKey();
        var dsid = pp.getW().pow(esk);
        var vectorH = providerPublicKey.getH(this.pp, promotionParameters);
        var vectorR = zp.getUniformlyRandomElements(pp.getNumEskDigits());
        var newPointsVector = RingElementVector.fromStream(newPoints.stream().map(e -> pp.getBg().getZn().createZnElement(e)));


        /* Compute pseudorandom values */
        // As in credit-earn, we use the PRF to make the algorithm deterministic
        var R = computeSpendDeductRandomness(userKeyPair.getSk(), token);

        // Prepare a new commitment (cPre0, cPre1) based on the pseudorandom values
        var exponents = new RingElementVector(R.tS, usk, R.eskUsrS, R.dsrnd0S, R.dsrnd1S, R.zS).concatenate(newPointsVector);
        var cPre0 = vectorH.innerProduct(exponents).pow(R.uS).compute();
        var cPre1 = pp.getG1Generator().pow(R.uS).compute();

        /* Enable double-spending-protection by forcing usk and esk becoming public in that case
           If token is used twice in two different transactions, the provider observes (c0,c1), (c0',c1') with gamma!=gamma'
           Hence, the provider can easily retrieve usk and esk (using the Schnorr-trick, computing (c0-c0')/(gamma-gamma') for usk, analogously for esk). */
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var gamma = Util.hashGamma(zp, dsid, tid, cPre0, cPre1, tid);
        var c0 = usk.mul(gamma).add(token.getDoubleSpendRandomness0());
        var c1 = esk.mul(gamma).add(token.getDoubleSpendRandomness1());

        /* Compute El-Gamal encryption of esk^*_usr using under secret key esk
           This allows the provider to decrypt usk^*_usr in case of double spending with the leaked esk.
           By additionally storing esk^*_prov, the provider can retrieve esk^* and thus iteratively decrypt the new esks. */

        // Decompose the encryption-secret-key to base eskDecBase and map the digits to Zn
        var eskUsrSDecBigInt = IntegerRing.decomposeIntoDigits(R.eskUsrS.asInteger(), pp.getEskDecBase().asInteger(), pp.getNumEskDigits());
        var eskUsrSDec = RingElementVector.generate(i -> zp.valueOf(eskUsrSDecBigInt[i]), eskUsrSDecBigInt.length);

        // Encrypt digits using El-Gamal and the randomness r
        var cTrace0 = pp.getW().pow(vectorR).compute();
        var cTrace1 = cTrace0.pow(esk).op(pp.getW().pow(eskUsrSDec)).compute();

        /* Build non-interactive (Fiat-Shamir transformed) ZKP to ensure that the user follows the rules of the protocol */
        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey);
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        var witness = new SpendDeductZkpWitnessInput(usk, token.getZ(), R.zS, token.getT(), R.tS, R.uS, esk, R.eskUsrS, token.getDoubleSpendRandomness0(), R.dsrnd0S, token.getDoubleSpendRandomness1(), R.dsrnd1S, eskUsrSDec, vectorR, token.getPoints(), newPointsVector);
        var commonInput = new SpendDeductZkpCommonInput(gamma, c0, c1, dsid, cPre0, cPre1, token.getCommitment0(), cTrace0, cTrace1);
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        // Assemble request
        return new SpendRequest(dsid, proof, c0, c1, cPre0, cPre1, cTrace0, cTrace1, token.getCommitment0(), token.getSignature());
    }

    /**
     * React to a legitimate spend request to allow the user retrieving an updated token with the value decreased by k.
     * Returns additional data for double-spending protection.
     *
     * @param spendRequest          the user's request
     * @param promotionParameters   specifying the promotion that the user wants to spend his points on (i.e. her claim)
     * @param providerKeyPair       keypair of the provider
     * @param tid                   transaction id, should be verified by the provider
     * @param spendDeductTree       the zero knowledge proof to verify for this promotion
     * @param userChoice            byte representation of the user choice,
     *                              influences the computation of challenge generator gamma and provider share esk_prov
     * @return tuple of response to send to the user and information required for double-spending protection
     */
    public DeductOutput generateSpendRequestResponse(PromotionParameters promotionParameters,
                                                     SpendRequest spendRequest,
                                                     ProviderKeyPair providerKeyPair,
                                                     ZnElement tid,
                                                     SpendDeductTree spendDeductTree,
                                                     UniqueByteRepresentable userChoice) {

        /* Verify that the request is valid and well-formed */

        // Verify signature of the old token (C1 must be g1 according to ZKP in T2 paper. We omit the ZKP and use g1 instead of C1)
        var signatureValid = pp.getSpsEq().verify(providerKeyPair.getPk().getPkSpsEq(),
                spendRequest.getSigma(),
                spendRequest.getCommitmentC0(),
                pp.getG1Generator(),
                pp.getG1Generator().pow(promotionParameters.getPromotionId()) // Verify token from correct promotion
        );
        if (!signatureValid) {
            throw new IllegalArgumentException("Signature of the request is not valid!");
        }

        // Validate ZKP
        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerKeyPair.getPk());
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var gamma = Util.hashGamma(pp.getBg().getZn(), spendRequest.getDsid(), tid, spendRequest.getCPre0(), spendRequest.getCPre1(), userChoice);
        var commonInput = new SpendDeductZkpCommonInput(spendRequest, gamma);
        var proofValid = fiatShamirProofSystem.checkProof(commonInput, spendRequest.getSpendDeductZkp());
        if (!proofValid) {
            throw new IllegalArgumentException("ZKP of the request is not valid!");
        }

        /* Request is valid. Compute new blinded token and signature
        *
        * Retrieve esk*_prov via PRF.
        * Need to use double-spending ID of the spent token as well as the transaction ID in addition to preliminary commitment as input for the PRF
        * to ensure that different spendings of the same token lead to different esk_prov.
        *
        * Also need to include object modelling the reward that the user chose to claim.
        * This is done by including a PRFtoZn image hashedClaim of the hashed spend-deduct tree in the spend request.
        */
        var preimage = new ByteArrayAccumulator();
        preimage.escapeAndSeparate(commonInput.c0Pre);
        preimage.escapeAndSeparate(commonInput.c1Pre);
        preimage.escapeAndSeparate(spendRequest.getDsid());
        preimage.escapeAndSeparate(tid);
        preimage.escapeAndSeparate(userChoice);
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
        return new DeductOutput(
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
     * @param userKeyPair       keypair of the user
     * @return token with the value of the old token + k
     */
    public Token handleSpendRequestResponse(PromotionParameters promotionParameters,
                                            SpendResponse spendResponse,
                                            SpendRequest spendRequest,
                                            Token token,
                                            Vector<BigInteger> newPoints,
                                            ProviderPublicKey providerPublicKey,
                                            UserKeyPair userKeyPair
    ) {

        var newPointsVector = RingElementVector.fromStream(newPoints.stream().map(e -> pp.getBg().getZn().createZnElement(e)));

        // Re-compute pseudorandom values
        var R = computeSpendDeductRandomness(userKeyPair.getSk(), token);

        // Verify the signature on the new, blinded commitment
        var blindedCStar0 = spendRequest.getCPre0().op(providerPublicKey.getH().get(1).pow(spendResponse.getEskProvStar().mul(R.uS)));
        var blindedCStar1 = pp.getG1Generator().pow(R.uS); // Recompute, just to make sure
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
                blindedCStar0.pow(R.uS.inv()), // Unblind commitment
                pp.getG1Generator(), // Same as unblinded CStar1
                R.eskUsrS.add(spendResponse.getEskProvStar()), // esk^* is sum of user's and providers new esk
                R.dsrnd0S,
                R.dsrnd1S,
                R.zS,
                R.tS,
                token.getPromotionId(),
                new RingElementVector(newPointsVector),
                // Change representation of signature to match the un-blinded commitments
                (SPSEQSignature) pp.getSpsEq().chgRep(spendResponse.getSigma(), R.uS.inv(), providerPublicKey.getPkSpsEq())
        );
    }

    /*
     * end of the implementation of the Deduct {@literal <}-{@literal >}Spend protocol
     */



    /*
     * methods for offline double-spending detection
     */

    /**
     * Given two double-spending tags belonging to a detected double-spending attempt, this algorithm computes the key material of the suspected user
     * as well as tracing information used to trace further transactions resulting from the detected double-spending attempt.
     *
     * @param pp         public parameters of the respective incentive system instance
     * @param dsTag      tag of the first spend operation
     * @param dsTagPrime tag of the second spend operation
     * @return suspected user's key material + tracing information
     */
    public UserInfo link(IncentivePublicParameters pp, DoubleSpendingTag dsTag, DoubleSpendingTag dsTagPrime) {
        // computing dsblame, DLOG of the usk of the user blamed of double-spending
        ZnElement c0 = dsTag.getC0();
        ZnElement c0Prime = dsTagPrime.getC0();
        ZnElement c0Difference = c0.sub(c0Prime);

        ZnElement gamma = dsTag.getGamma();
        ZnElement gammaPrime = dsTagPrime.getGamma();
        ZnElement gammaDifference = gamma.sub(gammaPrime);

        ZnElement dsBlame = c0Difference.div(gammaDifference);

        // computing dstrace to trace further transactions resulting from the detected double-spending attempt
        ZnElement c1 = dsTag.getC1();
        ZnElement c1Prime = dsTagPrime.getC1();
        ZnElement c1Difference = c1.sub(c1Prime);

        ZnElement dsTrace = c1Difference.div(gammaDifference);

        // computing public key of the user blamed of double-spending
        UserPublicKey upk = new UserPublicKey(pp.getW().pow(dsBlame));

        // assemble and return output
        return new UserInfo(upk, dsBlame, dsTrace);
    }

    /**
     * Determines whether the user with public key upk was really found guilty of double spending or whether he was wrongly accused.
     *
     * @param pp      public parameters of the respective incentive system instance
     * @param dsBlame used to verify/falsify that accused user indeed double-spended
     * @param upk     public key of user accused of double-spending
     * @return true if and only if user is found guilty of double-spending
     */
    public boolean verifyDs(IncentivePublicParameters pp, ZnElement dsBlame, UserPublicKey upk) {
        return pp.getW().pow(dsBlame).equals(upk.getUpk());
    }

    /**
     * Computes remainder token dsids for some double-spending transaction T (remainder token of a transaction: token that resulted from that transaction)
     * and at the same time retrieves the next ElGamal encryption key (i.e. the one for the transaction T' after T) from the chain of keys.
     *
     * @param pp      public parameters of the respective incentive system instance
     * @param dsTrace ElGamal decryption key used for tracing the next ElGamal encryption key (= dsid of remainder token) in the chain
     * @param dsTag   double-spending tag associated to T, used to trace the remainder token
     * @return trace output, which consists of remainder token and the dstrace ElGamal secret key of T'
     */
    public TraceOutput trace(IncentivePublicParameters pp, ZnElement dsTrace, DoubleSpendingTag dsTag) {
        // extract values from passed objects to save references in the below for-loops
        Zn usedZn = pp.getBg().getZn();
        GroupElement w = pp.getW();
        GroupElementVector ctrace1 = dsTag.getCtrace0(); // this is no off-by-one error but due to naming inconsistency between our Spend-Deduct code and the ds protection algos. in the paper
        GroupElementVector ctrace2 = dsTag.getCtrace1();

        // compute user share of ElGamal encryption secret key esk
        ZnElement[] userEskShareDigits = new ZnElement[pp.getNumEskDigits()];
        for (int i = 0; i < pp.getNumEskDigits(); i++) { // getNumEskDigits returns the number of digits the esk consists of (rho in the paper)
            for (int b = 0; b < Setup.ESK_DEC_BASE; b++) {
                // search for DLOG (i-th digit of the user share of esk), beta from paper is b in code
                if (w.pow(b).equals(ctrace1.get(i).pow(dsTrace.neg()).op(ctrace2.get(i)))) {
                    userEskShareDigits[i] = usedZn.valueOf(b);
                    break;
                }
            }
        }

        // check whether all bits could be computed
        for (int i = 0; i < pp.getNumEskDigits(); i++) {
            if (userEskShareDigits[i] == null) {
                throw new RuntimeException("Could not find a fitting " + i + "-th digit for the user's share of esk.");
            }
        }

        // compute next dstrace
        ZnElement dsTraceStar = usedZn.getZeroElement();
        for (int i = 0; i < pp.getNumEskDigits(); i++) {
            dsTraceStar = dsTraceStar.add(userEskShareDigits[i].mul(usedZn.valueOf(Setup.ESK_DEC_BASE).pow(i)));
        }
        dsTraceStar = dsTraceStar.add(dsTag.getEskStarProv());

        // assemble and return output (new dsid and dstrace)
        return new TraceOutput(pp.getW().pow(dsTraceStar), dsTraceStar);
    }

    /*
     * end of crypto methods for double-spending detection
     */


    /*
     * double-spending database interface to be used by provider
     */

    /**
     * Adds a transaction's data (i.e. ID, challenge generator gamma, used token's dsid, ...) to the double-spending database.
     * Triggers further DB-side actions for tracing tokens and transactions resulting from a double-spending attempt if necessary.
     *
     * @param tid         transaction ID
     * @param dsid        double-spending ID of used token
     * @param dsTag       double-spending tag of used token (contains challenge generator gamma)
     * @param userChoice  string representing the reward that the user chose
     * @param dbHandler   reference to the object handling the database connectivity
     */
    public void dbSync(ZnElement tid, GroupElement dsid, DoubleSpendingTag dsTag, String userChoice, BigInteger promotionId, DatabaseHandler dbHandler) {
        System.out.println("Started database synchronization process.");
        // shorthands for readability
        ZnElement gamma = dsTag.getGamma();
        TransactionIdentifier taId = new TransactionIdentifier(tid, gamma);

        // make list for keeping track of identifiers of transactions that are invalidated over the course of the method
        ArrayList<TransactionIdentifier> invalidatedTasIdentifiers = new ArrayList<>();

        // first part of DBSync from 2020 incentive system paper: adding a new transaction

        // if transaction is not yet in the database
        boolean transactionWasAlreadyKnown = dbHandler.containsTransactionNode(taId);
        if (!transactionWasAlreadyKnown) {
            System.out.println("Transaction not found in database, will be added.");
            // add a corresponding transaction node to DB (which also contains the dstag)
            Transaction ta = new Transaction(true, tid, userChoice, promotionId, dsTag); // first parameter: validity of the transaction
            dbHandler.addTransactionNode(ta);
        }

        // if dsid of used token is not yet in DB
        if (!dbHandler.containsTokenNode(dsid)) {
            System.out.println("Spent token not found in database, will be added.");
            // add a corresponding token node to DB
            dbHandler.addTokenNode(dsid);
            // and make edge from dsid's token node to the node of the passed transaction
            dbHandler.addTokenTransactionEdge(dsid, taId);
        }
        // if dsid is already in DB but transaction was not before this call -> double-spending attempt detected!
        else if (!transactionWasAlreadyKnown) {
            System.out.println("Spent token found in database, double-spending protection mechanism triggered.");

            // make edge from dsid's token node to the node of the passed transaction
            dbHandler.addTokenTransactionEdge(dsid, taId);

            // attempt to retrieve user info associated to dsid
            UserInfo associatedUserInfo = null;
            try {
                associatedUserInfo = dbHandler.getUserInfo(dsid);
            } catch (NoSuchElementException e) {
                System.out.println("No user info associated with the spent token.");
            }

            // if the token node has no user info associated with it
            if (associatedUserInfo == null) {
                System.out.println("Retrieving all transactions that spent the passed token.");
                // retrieve all transaction that consumed the dsid
                ArrayList<Transaction> consumingTaList = dbHandler.getConsumingTransactions(dsid);

                // use two of them to compute the user info for this token (i.e. link the double-spending to a user)
                try {
                    System.out.println("Attempting to compute user info for passed token.");
                    DoubleSpendingTag firstTaTag = consumingTaList.get(0).getDsTag();
                    DoubleSpendingTag secondTaTag = consumingTaList.get(1).getDsTag();
                    UserInfo uInfo = this.link(this.pp, firstTaTag, secondTaTag);
                    dbHandler.addAndLinkUserInfo(
                            uInfo,
                            dsid
                    );
                } catch (Exception e) {
                    System.out.println("Cannot compute user info for passed token: need at least 2 consuming transactions");
                }
            }


            // invalidate transaction
            System.out.println("Marking transaction invalid.");
            dbHandler.invalidateTransaction(taId);
            invalidatedTasIdentifiers.add(taId);
        }

        // second part of DBSync: cascading invalidations
        System.out.println("Starting cascading invalidations.");

        // whenever a transaction is invalidated: invalidate all transactions that resulted from it (if any exist)
        while (!invalidatedTasIdentifiers.isEmpty()) {
            System.out.println("Processing invalidated transaction. " + invalidatedTasIdentifiers.size() + " pending.");

            TransactionIdentifier currentTaId = invalidatedTasIdentifiers.remove(0);
            System.out.println("Invalidated transaction " + currentTaId.toString() + " found.");

            System.out.println("Retrieving transaction data for " + currentTaId.toString() + " .");

            // retrieve transaction
            Transaction ta = dbHandler.getTransactionNode(currentTaId);

            System.out.println("Retrieving consumed token data (including user info).");

            // retrieve double-spending ID of token consumed by transaction and the corresponding user info
            GroupElement consumedDsid = dbHandler.getConsumedTokenDsid(currentTaId);
            UserInfo consumedDsidUserInfo = dbHandler.getUserInfo(consumedDsid); // cannot be null since user info is always computed for invalidated transactions before needed

            System.out.println("Tracing remainder token.");

            // use Trace to compute remainder token's dsid (remainder token: token that resulted from the currently considered transaction)
            TraceOutput traceOutput = this.trace(this.pp, consumedDsidUserInfo.getDsTrace(), ta.getDsTag());
            GroupElement dsidStar = traceOutput.getDsidStar();

            System.out.println("Traced remainder token.");

            // add remainder token dsid if not contained yet
            if (!dbHandler.containsTokenNode(dsidStar)) {
                System.out.println("Remainder token not contained yet, will be added.");
                dbHandler.addTokenNode(dsidStar);
            } else {
                System.out.println("Remainder token is already contained in the database.");
            }

            System.out.println("Linking user info to remainder token.");

            // associate corresponding user info with remainder token dsid
            UserInfo correspondingUserInfo = new UserInfo(
                    consumedDsidUserInfo.getUpk(),
                    consumedDsidUserInfo.getDsBlame(),
                    traceOutput.getDsTraceStar()
            );
            dbHandler.addAndLinkUserInfo(
                    correspondingUserInfo,
                    dsidStar
            );

            System.out.println("Making edge from " + currentTaId.toString() + " to traced remainder token.");

            // link current transaction with remainder token in database
            dbHandler.addTransactionTokenEdge(currentTaId, dsidStar);

            System.out.println("Invalidating all transactions that (directly or indirectly) consumed the traced remainder token of " + currentTaId.toString() + ".");

            // invalidate all transactions that consumed the remainder token or followed from a transaction consuming it
            ArrayList<Transaction> followingTransactions = dbHandler.getConsumingTransactions(dsidStar);
            System.out.println(followingTransactions.size() + " transactions consuming remainder token detected, need to be invalidated.");
            followingTransactions.forEach(currentTa -> {
                dbHandler.invalidateTransaction(
                        currentTa.getTaIdentifier()
                );
                invalidatedTasIdentifiers.add(currentTa.getTaIdentifier()); // add invalidated transaction to list so it will be processed
            });
        }

        System.out.println("Cascading invalidations terminated.");

        System.out.println("Finished database synchronization process.");
    }

    /*
     * end of double-spending database interface to be used by provider
     */

    /**
     * Helper function for pseudorandom values on user side to generate a request, and handle the response.
     *
     * @param userSecretKey       the user secret key is hashed for this, and contains the prf key
     * @param promotionParameters the promotion id is hashed to ensure values are unique for promotionIds
     * @return a data object with the pseudorandom values
     */
    private IssueJoinRandomness computeIssueJoinRandomness(UserSecretKey userSecretKey, PromotionParameters promotionParameters) {
        var prv = pp.getPrfToZn().hashThenPrfToZnVector(
                userSecretKey.getPrfKey(),
                userSecretKey,
                7,
                "IssueJoin" + promotionParameters.getPromotionId().toString() // Ensure randomness is unique for every promotion
        ).stream().map(ringElement -> (ZnElement) ringElement).collect(Collectors.toList());
        return new IssueJoinRandomness(prv.get(0), prv.get(1), prv.get(2), prv.get(3), prv.get(4), prv.get(5), prv.get(6));
    }

    /**
     * Helper function for pseudorandom values on user side to generate a request, and handle the response.
     *
     * @param userSecretKey the user secret key is hashed for this, and contains the prf key
     * @param token         the token is hashed to server as input for the prf
     * @return a data object with the pseudorandom values
     */
    private SpendDeductRandomness computeSpendDeductRandomness(UserSecretKey userSecretKey, Token token) {
        var prv = pp.getPrfToZn().hashThenPrfToZnVector(
                userSecretKey.getPrfKey(),
                token,
                6,
                "SpendDeduct"
        ).stream().map(ringElement -> (ZnElement) ringElement).collect(Collectors.toList());
        return new SpendDeductRandomness(prv.get(0), prv.get(1), prv.get(2), prv.get(3), prv.get(4), prv.get(5));
    }
}

/**
 * Data class for user randomness used in issue-join protocol.
 */
@AllArgsConstructor
class IssueJoinRandomness {
    final ZnElement eskUsr;
    final ZnElement dsrnd0;
    final ZnElement dsrnd1;
    final ZnElement z;
    final ZnElement t;
    final ZnElement u;
    final ZnElement blindGenesisR;
}

/**
 * Data class for user randomness used in spend-deduct protocol.
 */
@AllArgsConstructor
class SpendDeductRandomness {
    Zn.ZnElement eskUsrS;
    Zn.ZnElement dsrnd0S;
    Zn.ZnElement dsrnd1S;
    Zn.ZnElement zS;
    Zn.ZnElement tS;
    Zn.ZnElement uS;
}
