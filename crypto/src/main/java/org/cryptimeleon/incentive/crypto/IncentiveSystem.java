package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.common.ByteArrayImplementation;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.ecdsa.ECDSASignature;
import org.cryptimeleon.craco.sig.ecdsa.ECDSASignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentive.crypto.callback.IClearingDBHandler;
import org.cryptimeleon.incentive.crypto.callback.IRegistrationCouponDBHandler;
import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.callback.IStorePublicKeyVerificationHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpWitnessInput;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessCommonInput;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessWitness;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.hash.impl.SHA256HashFunction;
import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Contains all main algorithms of the incentive system according to 2020 incentive systems paper.
 */
public class IncentiveSystem {

    // public parameters
    public final IncentivePublicParameters pp;

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
    public static IncentivePublicParameters setup(int securityParameter, BilinearGroupChoice bilinearGroupChoice) {
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
    public ProviderKeyPair generateProviderKeyPair() {
        return Setup.providerKeyGen(this.pp);
    }

    /**
     * wrapper for the user key generation method from Setup
     *
     * @return fresh user key pair
     */
    public UserPreKeyPair generateUserPreKeyPair() {
        return Setup.userPreKeyGen(this.pp);
    }

    public StoreKeyPair generateStoreKeyPair() {
        return Setup.storeKeyGen();
    }

    @Deprecated
    public PromotionParameters legacyPromotionParameters() {
        return new PromotionParameters(BigInteger.ONE, 1);
    }

    /*
     * Registration
     */

    /**
     * Issue a ECDSA signature on a {@code (upk, id-info)} tuple and return an object containing all data, the signature
     * und the verification key.
     * <p>
     * It should be verified somehow that the id-info belong user who request a signature to such a tuple, since it will
     * be used to identify and penalize maliciously acting users.
     *
     * @param storeKeyPair  the keypair is used to 1. sign he tuple and 2. attach the verification key to the data
     * @param userPublicKey a userPublicKey that will be encoded in the user's tokens
     * @param userInfo      some data that identifies the user as a person, e.g. the ID number, its name and address, ...
     * @return the signed tuple with signature
     */
    public RegistrationCoupon signRegistrationCoupon(StoreKeyPair storeKeyPair, UserPublicKey userPublicKey, String userInfo) {
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        MessageBlock msg = constructRegistrationCouponMessageBlock(userPublicKey, userInfo);
        ECDSASignature internalSignature = (ECDSASignature) ecdsaSignatureScheme.sign(msg, storeKeyPair.getSk().getEcdsaSigningKey());
        return new RegistrationCoupon(userPublicKey, userInfo, storeKeyPair.getPk(), internalSignature);
    }

    /**
     * Verify the registration coupon signature and its public key
     *
     * @param registrationCoupon the registration coupon to verify
     * @param verificationHandler some callback method to verify the public key
     * @return whether the signature is valid and trusted
     */
    public boolean verifyRegistrationCoupon(RegistrationCoupon registrationCoupon, IStorePublicKeyVerificationHandler verificationHandler) {
        if (!verificationHandler.isStorePublicKeyTrusted(registrationCoupon.getStorePublicKey())) {
            throw new RuntimeException("Store Public Key is not Trusted");
        }

        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        MessageBlock msg = constructRegistrationCouponMessageBlock(registrationCoupon.getUserPublicKey(), registrationCoupon.getUserInfo());
        return ecdsaSignatureScheme.verify(msg, registrationCoupon.getSignature(), registrationCoupon.getStorePublicKey().getEcdsaVerificationKey());
    }

    /**
     * Issue a registration token i.e., a signature of the user public key under the providers registration SPSEQ keys,
     * for a valid, signed registrationCoupon,
     * <p>
     * Verifies that the user data is signed under a valid and trusted public key.
     * Allows storing the user data using a callback.
     *
     * @param providerKeyPair     The keypair of the provider used to issue the SPSEQ signature
     * @param registrationCoupon  the coupon holds the user data, store public key, and signature under that key
     * @param verificationHandler an object that provides a function to verify the store public key
     * @param dbAccess            an object that provides a function to store the {@code registrationCoupon} to identify users and/or
     *                            hold stores accountable for registering fake users
     * @return a SPSEQSignature on the users public key under the registration SPSEQ keys
     */
    public SPSEQSignature verifyRegistrationCouponAndIssueRegistrationToken(ProviderKeyPair providerKeyPair,
                                                                            RegistrationCoupon registrationCoupon,
                                                                            IStorePublicKeyVerificationHandler verificationHandler,
                                                                            IRegistrationCouponDBHandler dbAccess) {
        // 1. Verify verification key, e.g. with a CA hierarchy or whitelist
        if (!verificationHandler.isStorePublicKeyTrusted(registrationCoupon.getStorePublicKey())) {
            throw new RuntimeException("Store Public Key is not Trusted");
        }

        // 2. Verify Signature on User Data
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        MessageBlock msg = constructRegistrationCouponMessageBlock(registrationCoupon.getUserPublicKey(), registrationCoupon.getUserInfo());
        if (!ecdsaSignatureScheme.verify(msg, registrationCoupon.getSignature(), registrationCoupon.getStorePublicKey().getEcdsaVerificationKey())) {
            throw new RuntimeException("Registration Coupon Invalid");
        }

        // 3. Store user info
        dbAccess.storeUserData(registrationCoupon);

        // 4. Sign userPublicKey under registration (registration) keys
        return (SPSEQSignature) pp.getSpsEq().sign(
                providerKeyPair.getSk().getRegistrationSpsEqSk(),
                registrationCoupon.getUserPublicKey().getUpk(),
                pp.getW());
    }

    /**
     * Verify the registration token signature.
     *
     * @param providerPublicKey          the public key of the provider used for verification
     * @param registrationTokenSignature the registration token signature
     * @param userPublicKey the user public key that is signed in this registration token
     * @return whether the signature is valid
     */
    public boolean verifyRegistrationToken(ProviderPublicKey providerPublicKey,
                                           SPSEQSignature registrationTokenSignature,
                                           UserPublicKey userPublicKey) {
        return pp.getSpsEq().verify(
                providerPublicKey.getRegistrationSpsEqPk(),
                registrationTokenSignature,
                userPublicKey.getUpk(),
                pp.getW()
        );
    }

    /**
     * Verify the registration token signature.
     *
     * @param providerPublicKey          the public key of the provider used for verification
     * @param registrationTokenSignature the registration token signature
     * @param registrationCoupon         the registration token containing the signed data
     * @return whether the signature is valid
     */
    public boolean verifyRegistrationToken(ProviderPublicKey providerPublicKey,
                                           SPSEQSignature registrationTokenSignature,
                                           RegistrationCoupon registrationCoupon) {
        return verifyRegistrationToken(providerPublicKey, registrationTokenSignature, registrationCoupon.getUserPublicKey());
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
    public JoinFirstStepOutput generateJoinRequest(ProviderPublicKey pk, UserKeyPair ukp) {
        UserPublicKey upk = ukp.getPk();
        UserSecretKey usk = ukp.getSk();

        // generate random values needed for generation of fresh user token using PRF hashThenPRFtoZn, user secret key is hash input
        IssueJoinRandomness R = IssueJoinRandomness.generate(pp);

        // blind registration signature
        GroupElement blindedUpk = upk.getUpk().pow(R.blindRegistrationSignatureR);
        GroupElement blindedW = pp.getW().pow(R.blindRegistrationSignatureR);
        SPSEQSignature blindedRegistrationSignature = (SPSEQSignature) pp.getSpsEq().chgRep(
                ukp.getSk().getRegistrationSignature(),
                R.blindRegistrationSignatureR,
                pk.getRegistrationSpsEqPk()
        );
        assert pp.getSpsEq().verify(pk.getRegistrationSpsEqPk(), blindedRegistrationSignature, blindedUpk, blindedW);


        GroupElementVector H = pk.getTokenMetadataH(this.pp);

        // compute Pedersen commitment for user token
        // need to retrieve exponent from usk object; point count of 0 is reresented by zero in used Z_n
        RingElementVector exponents = new RingElementVector(R.t, usk.getUsk(), R.dsidUser, R.dsrnd, R.z);
        GroupElement c0Pre = H.innerProduct(exponents).pow(R.u);
        GroupElement c1Pre = pp.getG1Generator().pow(R.u);

        // compute NIZKP to prove well-formedness of token
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pk));
        CommitmentWellformednessCommonInput cwfCommon = new CommitmentWellformednessCommonInput(c0Pre, c1Pre, blindedUpk, blindedW);
        CommitmentWellformednessWitness cwfWitness = new CommitmentWellformednessWitness(usk.getUsk(), R.dsidUser, R.dsrnd, R.z, R.t, R.u.inv());
        FiatShamirProof cwfProof = cwfProofSystem.createProof(cwfCommon, cwfWitness);

        // assemble and return join request object (commitment, proof of well-formedness)
        return JoinFirstStepOutput.of(
                R,
                new JoinRequest(c0Pre, c1Pre, cwfProof, blindedUpk, blindedW, blindedRegistrationSignature)
        );
    }

    /**
     * Implements the functionality of the Issue algorithm of the Cryptimeleon incentive system, i.e. handles a join request by signing the
     * included preliminary commitment after adding the provider's share for the tracking key esk.
     *
     * @param pkp key pair of the provider
     * @param jr  join request to be handled
     * @return join response, i.e. object representing the third message in the Issue-Join protocol
     * @throws IllegalArgumentException indicating that the proof for commitment well-formedness was rejected
     */
    public JoinResponse generateJoinRequestResponse(PromotionParameters promotionParameters, ProviderKeyPair pkp, JoinRequest jr) throws IllegalArgumentException {
        ProviderPublicKey pk = pkp.getPk();
        ProviderSecretKey sk = pkp.getSk();

        // read out parts of the pre-commitment and the commitment well-formedness proof from the join request object
        GroupElement c0Pre = jr.getPreCommitment0();
        GroupElement c1Pre = jr.getPreCommitment1();
        GroupElement blindedW = jr.getBlindedW();
        GroupElement blindedUpk = jr.getBlindedUpk();
        FiatShamirProof cwfProof = jr.getCwfProof();

        // Verify registration signature
        SPSEQSignature blindedRegistrationSignature = jr.getBlindedRegistrationSignature();
        if (!pp.getSpsEq().verify(pk.getRegistrationSpsEqPk(), blindedRegistrationSignature, blindedUpk, blindedW)) {
            throw new IllegalArgumentException("The blinded registration signature is invalid!");
        }

        // reassemble common input for the commitment well-formedness proof
        CommitmentWellformednessCommonInput cwfProofCommonInput = new CommitmentWellformednessCommonInput(c0Pre, c1Pre, blindedUpk, blindedW);

        // check commitment well-formedness proof for validity
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pk));
        if (!cwfProofSystem.checkProof(cwfProofCommonInput, cwfProof)) {
            throw new IllegalArgumentException("The proof of the commitment being well-formed was rejected.");
        }

        // modify pre-commitment 0 using homomorphism trick and randomly chosen exponent
        ZnElement dsidProv = pp.getBg().getZn().getUniformlyRandomElement();
        GroupElement modifiedC0Pre = c0Pre.op(c1Pre.pow(sk.getQ().get(1).mul(dsidProv)));

        // create certificate for modified pre-commitment vector
        SPSEQSignature cert = (SPSEQSignature) pp.getSpsEq().sign(
                sk.getSkSpsEq(),
                modifiedC0Pre,
                c1Pre,
                c1Pre.pow(promotionParameters.getPromotionId())
        ); // first argument: signing keys, other arguments form the msg vector

        // assemble and return join response object
        return new JoinResponse(cert, dsidProv);
    }

    /**
     * Implements the second part of the functionality of the Issue algorithm from the Cryptimeleon incentive system, i.e. computes the final user data
     * (token and corresponding certificate) from the signed preliminary token from the passed join request and response.
     *
     * @param pk                  public key of the provider the user interacted with
     * @param joinFirstStepOutput the initial join output containing the internal randomness and the request sent to the provider
     * @param jRes                join response to be handled
     * @return token containing 0 points
     */
    public Token handleJoinRequestResponse(PromotionParameters promotionParameters, ProviderPublicKey pk, JoinFirstStepOutput joinFirstStepOutput, JoinResponse jRes) {
        // re-generate random values from join request generation of fresh user token using PRF hashThenPRFtoZn, user secret key is hash input
        IssueJoinRandomness R = joinFirstStepOutput.getIssueJoinRandomness();
        JoinRequest jReq = joinFirstStepOutput.getJoinRequest();

        // extract relevant variables from join request, join response and public parameters
        GroupElement c0Pre = jReq.getPreCommitment0();
        SPSEQSignature preCert = jRes.getPreCertificate();
        SPSEQSignatureScheme usedSpsEq = pp.getSpsEq();

        GroupElement c0PreWithDsidProv = c0Pre.op(pk.getH().get(1).pow(jRes.getDsidProv()).pow(R.u));

        // verify the signature on the modified pre-commitment
        if (!usedSpsEq.verify(
                pk.getPkSpsEq(),
                preCert,
                c0PreWithDsidProv,
                jReq.getPreCommitment1(),
                jReq.getPreCommitment1().pow(promotionParameters.getPromotionId())
        )) {
            throw new RuntimeException("signature on pre-commitment's left part is not valid!");
        }

        // change representation of token-certificate pair
        SPSEQSignature finalCert = (SPSEQSignature) usedSpsEq.chgRep(preCert, R.u.inv(), pk.getPkSpsEq()); // adapt signature
        GroupElement finalCommitment0 = c0PreWithDsidProv.pow(R.u.inv()); // need to adapt message manually (entry by entry), used equivalence relation is R_exp
        GroupElement finalCommitment1 = pp.getG1Generator();

        // assemble and return token
        Zn usedZn = pp.getBg().getZn();
        RingElementVector zeros = RingElementVector.generate(usedZn::getZeroElement, promotionParameters.getPointsVectorSize());

        return new Token(finalCommitment0, finalCommitment1, R.dsidUser.add(jRes.getDsidProv()), R.dsrnd, R.z, R.t, promotionParameters.getPromotionId(), zeros, finalCert);
    }

    /*
     * end of the implementation of the Issue {@literal <}-{@literal >}Join protocol
     */

    /**
     * Generate the request to the store for getting an 'earn coupon' for a specific promotion and basket.
     *
     * @param token earn points for this token
     * @param userKeyPair only required for pseudorandomness
     * @param basketId the id of the basket that this earn-request will be associated to
     * @param promotionId the id of the promotion of this token and earn request
     * @return a request that can be processed by the store
     */
    public EarnStoreRequest generateEarnCouponRequest(Token token, UserKeyPair userKeyPair, UUID basketId, BigInteger promotionId) {
        // Compute pseudorandom value from the token that is used to blind the commitment
        // This makes this algorithm deterministic
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn");

        var c0Prime = token.getCommitment0().pow(s).compute();
        var c1Prime = token.getCommitment1().pow(s).compute();
        var c2Prime = c1Prime.pow(token.getPromotionId()).compute();

        byte[] h = computeEarnHash(c0Prime, c1Prime, c2Prime);

        return new EarnStoreRequest(h, basketId, promotionId);
    }

    /**
     * Method for the store to issue an earn coupon for a verified vector of points to earn for a fixed request and basket.
     * It needs to be verified that the basket is not yet redeemed for a request with different hash (we allow re-requesting
     * the same update in case of failure). Further, the basket needs to be marked as redeemed by this request.
     *
     * @param storeKeyPair the keypair of the store required to issue the signature of the coupon
     * @param deltaK a verified amount of points the user is eligible to earn for the request
     * @param earnStoreRequest the users request containing all necessary data for the store to verify the request and issue the coupon
     * @param storeBasketRedeemedHandler a callback to 1) Check that this basket can be redeemed with this request 2) Marks this basket as redeemed for this request
     * @return the 'coupon', which is essentially a signature on the earn update data under the stores ECDSA key
     */
    public EarnStoreCouponSignature signEarnCoupon(StoreKeyPair storeKeyPair,
                                                   Vector<BigInteger> deltaK,
                                                   EarnStoreRequest earnStoreRequest,
                                                   IStoreBasketRedeemedHandler storeBasketRedeemedHandler) {
        boolean issueSignature = storeBasketRedeemedHandler.verifyAndStorePromotionIdAndHashForBasket(earnStoreRequest.getBasketId(), earnStoreRequest.getPromotionId(), earnStoreRequest.getH());
        if (!issueSignature) {
            throw new RuntimeException(String.format("Basket %s already redeemed with different hash than %s!", earnStoreRequest.getBasketId().toString(), Arrays.toString(earnStoreRequest.getH())));
        }

        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        var message = constructEarnCouponMessageBlock(earnStoreRequest.getPromotionId(), deltaK, earnStoreRequest.getH());
        var signature = (ECDSASignature) ecdsaSignatureScheme.sign(message, storeKeyPair.getSk().getEcdsaSigningKey());

        return new EarnStoreCouponSignature(signature, storeKeyPair.getPk());
    }

    /**
     * Verify that an earn store coupon is signed under a trusted public key.
     *
     * @param earnStoreRequest                  the requests which holds most data of the coupon
     * @param deltaK                            the earn-amount of the coupon
     * @param earnStoreCouponSignature          the signature of the coupon including the verification key
     * @param storePublicKeyVerificationHandler a callback to verify that the verification key is trusted
     * @return true if all checks pass
     */
    public boolean verifyEarnCoupon(EarnStoreRequest earnStoreRequest,
                                    Vector<BigInteger> deltaK,
                                    EarnStoreCouponSignature earnStoreCouponSignature,
                                    IStorePublicKeyVerificationHandler storePublicKeyVerificationHandler) {

        // Verify Store ECDSA public key is trusted
        if (!storePublicKeyVerificationHandler.isStorePublicKeyTrusted(earnStoreCouponSignature.getStorePublicKey())) {
            throw new RuntimeException("Store public key is not trusted");
        }

        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        var message = constructEarnCouponMessageBlock(earnStoreRequest.getPromotionId(), deltaK, earnStoreRequest.getH());
        return ecdsaSignatureScheme.verify(message, earnStoreCouponSignature.getSignature(), earnStoreCouponSignature.getStorePublicKey().getEcdsaVerificationKey());
    }

    /**
     * Generate a earn request that uses a earn coupon signed by a store as a proof that one is eligigble to earn points.
     *
     * @param token the token to update, must be the same as used for the coupon
     * @param providerPublicKey the public key of the provider required for blinding the tokens signature
     * @param userKeyPair the user keypair
     * @param promotionId the promotion id of the token
     * @param deltaK the vector of points that shall be added to the tokens points. Must match the amount signed by the {@literal earnStoreCouponSignature}
     * @param earnStoreCouponSignature the signature of a trusted store admitting this earn request
     * @return the request to send to the provider
     */
    public EarnRequestECDSA generateEarnRequest(
            Token token,
            ProviderPublicKey providerPublicKey,
            UserKeyPair userKeyPair,
            BigInteger promotionId,
            Vector<BigInteger> deltaK,
            EarnStoreCouponSignature earnStoreCouponSignature
    ) {
        // Re-compute pseudorandom blinding value
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn");

        // Blind commitments and change representation of signature such that it is valid for blinded commitments
        // The blinded commitments and signature are sent to the provider
        return new EarnRequestECDSA(
                promotionId,
                deltaK,
                earnStoreCouponSignature,
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
     * Providers logic for handling a earn request. Does the following:
     * 1. Verify request authenticated by trusted store
     * 2. Verify token valid
     * 3. Update token to new score
     * 4. Store clearing data to make store 'pay' for issuing the points
     *
     * @param earnRequestECDSA                  the earn request
     * @param promotionParameters               the parameters associated to the promotion from the user's request. Can be identified
     *                                          by the promotionId in the request
     * @param providerKeyPair                   the keys of the provider
     * @param clearingDBHandler                 a callback for adding all relevant data to the clearing db
     * @param storePublicKeyVerificationHandler a callback for verifying that the store's key used to authenticate the request is trusted
     * @return the blindly updated signature
     */
    public SPSEQSignature generateEarnResponse(EarnRequestECDSA earnRequestECDSA, PromotionParameters promotionParameters,
                                               ProviderKeyPair providerKeyPair,
                                               IClearingDBHandler clearingDBHandler,
                                               IStorePublicKeyVerificationHandler storePublicKeyVerificationHandler) {
        // Blinded token
        var c0Prime = earnRequestECDSA.getcPrime0();
        var c1Prime = earnRequestECDSA.getcPrime1();
        var c2Prime = earnRequestECDSA.getcPrime1().pow(earnRequestECDSA.getPromotionId());

        // Compute hash h
        var h = computeEarnHash(c0Prime, c1Prime, c2Prime);

        // Verify Store ECDSA public key is trusted
        if (!storePublicKeyVerificationHandler.isStorePublicKeyTrusted(earnRequestECDSA.getEarnStoreCoupon().getStorePublicKey())) {
            throw new RuntimeException("Store public key is not trusted");
        }

        // Verify ECDSA
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        var message = constructEarnCouponMessageBlock(earnRequestECDSA.getPromotionId(), earnRequestECDSA.getDeltaK(), h);
        var ecdsaValid = ecdsaSignatureScheme.verify(message, earnRequestECDSA.getEarnStoreCoupon().getSignature(), earnRequestECDSA.getEarnStoreCoupon().getStorePublicKey().getEcdsaVerificationKey());
        if (!ecdsaValid) throw new RuntimeException("ECDSA signature invalid");

        // Verify blinded SPSEQ
        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();
        var blindedSpseqValid = spseqSignatureScheme.verify(providerKeyPair.getPk().getPkSpsEq(), earnRequestECDSA.getSpseqSignature(), GroupElementVector.of(c0Prime, c1Prime, c2Prime));
        if (!blindedSpseqValid) throw new RuntimeException("(Blinded) SPSEQ signature invalid");

        // Add to clearing DB
        clearingDBHandler.addEarningDataToClearingDB(earnRequestECDSA, h);

        // Blind-sign update
        var Q = providerKeyPair.getSk().getTokenPointsQ(promotionParameters);
        var K = earnRequestECDSA.getDeltaK().map(k -> pp.getBg().getG1().getZn().createZnElement(k));
        var c0PrimePlusDeltaK = c0Prime.op(c1Prime.pow(Q.innerProduct(K))).compute();

        return (SPSEQSignature) spseqSignatureScheme.sign(providerKeyPair.getSk().getSkSpsEq(), c0PrimePlusDeltaK, c1Prime, c2Prime);
    }

    /**
     * Obtains the updated token with more points from the blinded updated signature retrieved from the provider.
     *
     * @param earnRequest the request sent to the provider
     * @param changedSignature the response signature
     * @param promotionParameters the parameters of this promotion
     * @param token the old token
     * @param userKeyPair the user keypair
     * @param providerPublicKey the provider's public key
     * @return the updated token with more points
     */
    public Token handleEarnResponse(EarnRequestECDSA earnRequest,
                                    SPSEQSignature changedSignature,
                                    PromotionParameters promotionParameters,
                                    Token token,
                                    UserKeyPair userKeyPair,
                                    ProviderPublicKey providerPublicKey) {

        // Pseudorandom randomness s used for blinding in the request
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn");
        var K = RingElementVector.fromStream(earnRequest.getDeltaK().stream().map(e -> pp.getBg().getZn().createZnElement(e)));

        // Recover blinded commitments (to match the commitments signed by the prover) with updated value
        var blindedNewC0 = earnRequest.getcPrime0()
                .op(providerPublicKey.getTokenPointsH(promotionParameters)
                        .pow(s)
                        .innerProduct(K)
                ).compute();
        var blindedNewC1 = earnRequest.getcPrime1();

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
                token.getDoubleSpendingId(),
                token.getDoubleSpendRandomness(),
                token.getZ(),
                token.getT(),
                token.getPromotionId(),
                new RingElementVector(token.getPoints().zip(K, RingElement::add)),
                (SPSEQSignature) newSignature
        );
    }

    private static byte[] computeEarnHash(GroupElement c0Prime, GroupElement c1Prime, GroupElement c2Prime) {
        var sha = new SHA256HashFunction();
        var hashAccumulator = new ByteArrayAccumulator();
        hashAccumulator.escapeAndAppend(c0Prime);
        hashAccumulator.escapeAndAppend(c1Prime);
        hashAccumulator.escapeAndAppend(c2Prime);
        return sha.hash(hashAccumulator.extractBytes());
    }

    private static MessageBlock constructEarnCouponMessageBlock(BigInteger promotionId, Vector<BigInteger> deltaK, byte[] h) {
        return new MessageBlock(
                new ByteArrayImplementation("earn".getBytes()),
                new ByteArrayImplementation(promotionId.toByteArray()),
                new MessageBlock(deltaK.map((k) -> new ByteArrayImplementation(k.toByteArray())).toList()),
                new ByteArrayImplementation(h)
        );
    }


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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
                token.getDoubleSpendingId(),
                token.getDoubleSpendRandomness(),
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

    public SpendCouponRequest generateStoreSpendRequest(Token token, UserKeyPair userKeyPair, Vector<BigInteger> newPoints, ProviderPublicKey providerPublicKey, PromotionParameters promotionParameters, UUID basketId, SpendDeductTree spendDeductTree) {
        // TODO context=spendDeductTree?
        var R = computeSpendDeductRandomness(userKeyPair.getSk(), token);
        var zp = pp.getBg().getZn();
        var usk = userKeyPair.getSk().getUsk();
        var vectorH = providerPublicKey.getH(this.pp, promotionParameters);
        var newPointsVector = RingElementVector.fromStream(newPoints.stream().map(e -> pp.getBg().getZn().createZnElement(e)));

        // Prepare a new commitment (cPre0, cPre1) based on the pseudorandom values
        var exponents = new RingElementVector(R.tS, usk, R.dsidUserS, R.dsrndS, R.zS).concatenate(newPointsVector);
        var cPre0 = vectorH.innerProduct(exponents).pow(R.uS).compute();
        var cPre1 = pp.getG1Generator().pow(R.uS).compute();
        var cPre2 = pp.getG1Generator().pow(R.uS).pow(promotionParameters.getPromotionId()).compute();

        var gamma = Util.hashGamma(zp, token.getDoubleSpendingId(), basketId, cPre0, cPre1, cPre2); // TODO include all user choices
        var c = usk.mul(gamma).add(token.getDoubleSpendRandomness());

        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey);
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        var witness = new SpendDeductZkpWitnessInput(usk, token.getZ(), R.zS, token.getT(), R.tS, R.uS, R.dsidUserS, token.getDoubleSpendRandomness(), R.dsrndS, token.getPoints(), newPointsVector);
        var commonInput = new SpendDeductZkpCommonInput(gamma, c, token.getDoubleSpendingId(), cPre0, cPre1, token.getCommitment0());
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        return new SpendCouponRequest(token.getDoubleSpendingId(), c, token.getSignature(), token.getCommitment0(), cPre0, cPre1, proof);
    }

    public SpendStoreOutput signSpendCoupon(StoreKeyPair storeKeyPair,
                                            ProviderPublicKey providerPublicKey,
                                            UUID basketId,
                                            PromotionParameters promotionParameters,
                                            SpendCouponRequest spendCouponRequest,
                                            SpendDeductTree spendDeductTree,
                                            IStoreBasketRedeemedHandler iStoreBasketRedeemedHandler
    ) {
        var zp = pp.getBg().getZn();

        // Verify old token signature valid
        var c0 = spendCouponRequest.getC0();
        var c1 = pp.getG1Generator();
        var c2 = c1.pow(promotionParameters.getPromotionId());

        var spseqValid = pp.getSpsEq().verify(providerPublicKey.getPkSpsEq(), spendCouponRequest.getSigma(), c0, c1, c2);
        if (!spseqValid) {
            throw new RuntimeException("Invalid token signature");
        }

        // Compute gamma
        var cPre0 = spendCouponRequest.getCPre0();
        var cPre1 = spendCouponRequest.getCPre1();
        var cPre2 = cPre1.pow(promotionParameters.getPromotionId()).compute();

        var gamma = Util.hashGamma(zp, spendCouponRequest.getDsid(), basketId, cPre0, cPre1, cPre2); // TODO include all user choices

        // Verify proof
        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey);
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        var commonInput = new SpendDeductZkpCommonInput(spendCouponRequest, gamma);
        var proofValid = fiatShamirProofSystem.checkProof(commonInput, spendCouponRequest.getSpendZkp());
        if (!proofValid) {
            throw new IllegalArgumentException("ZKP of the request is not valid!");
        }

        // Signature to legitimate retrieving a new token
        var ecdsa = new ECDSASignatureScheme();
        MessageBlock spendCouponMessageBlock = constructSpendCouponMessageBlock(promotionParameters.getPromotionId(), spendCouponRequest.getDsid(), basketId);
        var signature = (ECDSASignature) ecdsa.sign(spendCouponMessageBlock, storeKeyPair.getSk().getEcdsaSigningKey());
        var spendCouponSignature = new SpendCouponSignature(signature, storeKeyPair.getPk());

        // TODO (optional): Check if dsid already used at provider => direct rejection)

        var redeemResult = iStoreBasketRedeemedHandler.verifyAndRedeemBasket(basketId, promotionParameters.getPromotionId(), gamma, spendCouponSignature);
        if (redeemResult instanceof IStoreBasketRedeemedHandler.BasketRedeemedForDifferentGamma) {
            throw new RuntimeException("Basket already redeemed for different request!");
        }
        if (redeemResult instanceof IStoreBasketRedeemedHandler.BasketRedeemedForSameGamma) {
            // TODO there might be a nicer way around this?
            return ((IStoreBasketRedeemedHandler.BasketRedeemedForSameGamma) redeemResult).spendStoreOutput;
        }
        if (redeemResult instanceof IStoreBasketRedeemedHandler.BasketNotRedeemed) {
            // pass, everything allright
        }

        // 3. Blacklist dsid at provider and send clearing data => Provider finds users that perform double-spending attack!
        var spendClearingData = new SpendClearingData(
                promotionParameters.getPromotionId(),
                spendCouponRequest.getDsid(),
                basketId,
                spendCouponRequest.getSigma(),
                signature,
                storeKeyPair.getPk(),
                spendCouponRequest.getC(),
                spendCouponRequest.getC0(),
                spendCouponRequest.getCPre0(),
                spendCouponRequest.getCPre1(),
                spendCouponRequest.getSpendZkp()
        );

        // 1. Offline: Wait for payment
        // 2. Issue reward
        // 3. Give coupon signature to user

        return new SpendStoreOutput(spendCouponSignature, spendClearingData);
    }

    public boolean verifySpendCouponSignature(SpendCouponRequest spendCouponRequest, SpendCouponSignature spendCouponSignature, PromotionParameters promotionParameters, UUID basketId) {
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        MessageBlock messageBlock = constructSpendCouponMessageBlock(promotionParameters.getPromotionId(), spendCouponRequest.getDsid(), basketId);
        return ecdsaSignatureScheme.verify(messageBlock, spendCouponSignature.getSignature(), spendCouponSignature.getStorePublicKey().getEcdsaVerificationKey());
    }

    public SpendResponseECDSA verifySpendRequestAndIssueNewToken(ProviderKeyPair providerKeyPair,
                                                                 SpendRequestECDSA spendRequestECDSA,
                                                                 PromotionParameters promotionParameters,
                                                                 IStorePublicKeyVerificationHandler storePublicKeyVerificationHandler,
                                                                 SpendDeductTree spendDeductTree) {

        // 1. Verify Store ECDSA public key is trusted
        if (!storePublicKeyVerificationHandler.isStorePublicKeyTrusted(spendRequestECDSA.getStorePublicKey())) {
            throw new RuntimeException("Store public key is not trusted");
        }

        // 2. Verify ECDSA
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        MessageBlock messageBlock = constructSpendCouponMessageBlock(promotionParameters.getPromotionId(), spendRequestECDSA.getDoubleSpendingId(), spendRequestECDSA.getBasketId());
        boolean ecdsaValid = ecdsaSignatureScheme.verify(messageBlock, spendRequestECDSA.getCouponSignature(), spendRequestECDSA.getStorePublicKey().getEcdsaVerificationKey());
        if (!ecdsaValid) {
            throw new RuntimeException("Invalid ECDSA signature!");
        }

        // 3. Verify SPSEQ
        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();
        spseqSignatureScheme.verify(providerKeyPair.getPk().getPkSpsEq(), spendRequestECDSA.getTokenSignature(), spendRequestECDSA.getC0(), pp.getG1Generator(), pp.getG1Generator().pow(promotionParameters.getPromotionId()));

        // 4. Verify NZIK
        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerKeyPair.getPk());
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var gamma = Util.hashGamma(pp.getBg().getZn(), spendRequestECDSA.getDoubleSpendingId(), spendRequestECDSA.getBasketId(), spendRequestECDSA.getcPre0(), spendRequestECDSA.getcPre1(), spendRequestECDSA.getcPre1().pow(promotionParameters.getPromotionId()));
        var commonInput = new SpendDeductZkpCommonInput(spendRequestECDSA, gamma);
        var proofValid = fiatShamirProofSystem.checkProof(commonInput, spendRequestECDSA.getProof());
        if (!proofValid) {
            throw new IllegalArgumentException("ZKP of the request is not valid!");
        }

        // 5. dsid_prov^*
        var preimage = new ByteArrayAccumulator();
        // TODO prf(coupon_signature) instead?
        preimage.escapeAndSeparate(spendRequestECDSA.getDoubleSpendingId());
        preimage.escapeAndSeparate(spendRequestECDSA.getBasketId().toString());
        preimage.escapeAndSeparate(spendRequestECDSA.getPromotionId().toByteArray());
        preimage.escapeAndSeparate(commonInput.c0Pre);
        preimage.escapeAndSeparate(commonInput.c1Pre);
        var dsidStarProv = pp.getPrfToZn().hashThenPrfToZn(providerKeyPair.getSk().getBetaProv(), new ByteArrayImplementation(preimage.extractBytes()), "dsid");

        // 6. Create signature for new token
        GroupElement cPre0 = spendRequestECDSA.getcPre0();
        GroupElement cPre1 = spendRequestECDSA.getcPre1();
        GroupElement cPre2 = cPre1.pow(promotionParameters.getPromotionId());
        SPSEQSignature updatedTokenSignature = (SPSEQSignature) spseqSignatureScheme.sign(
                providerKeyPair.getSk().getSkSpsEq(),
                cPre0.op(cPre1.pow(dsidStarProv.mul(providerKeyPair.getSk().getQ().get(1)))),
                cPre1,
                cPre2
        );

        // Add to DS-DB
        // TODO

        return new SpendResponseECDSA(updatedTokenSignature, dsidStarProv);
    }

    public Token retrieveUpdatedTokenFromSpendResponse(SpendRequestECDSA spendRequestECDSA,
                                                       SpendResponseECDSA spendResponseECDSA,
                                                       Vector<BigInteger> newPoints,
                                                       UserKeyPair userKeyPair,
                                                       Token token,
                                                       ProviderPublicKey providerPublicKey,
                                                       PromotionParameters promotionParameters) {
        var newPointsVector = RingElementVector.fromStream(newPoints.stream().map(e -> pp.getBg().getZn().createZnElement(e)));

        // Re-compute pseudorandom values
        var R = computeSpendDeductRandomness(userKeyPair.getSk(), token);

        // Verify the signature on the new, blinded commitment
        var blindedCStar0 = spendRequestECDSA.getcPre0().op(providerPublicKey.getH().get(1).pow(spendResponseECDSA.getDsidStarProv().mul(R.uS)));
        var blindedCStar1 = pp.getG1Generator().pow(R.uS);
        var blindedCStar2 = blindedCStar1.pow(promotionParameters.getPromotionId());
        var valid = pp.getSpsEq().verify(
                providerPublicKey.getPkSpsEq(),
                spendResponseECDSA.getSignature(),
                blindedCStar0,
                blindedCStar1,
                blindedCStar2
        );
        if (!valid) {
            throw new IllegalArgumentException("Signature is not valid");
        }

        // Build new token
        return new Token(
                blindedCStar0.pow(R.uS.inv()),
                pp.getG1Generator(),
                R.dsidUserS.add(spendResponseECDSA.getDsidStarProv()),
                R.dsrndS,
                R.zS,
                R.tS,
                token.getPromotionId(),
                new RingElementVector(newPointsVector),
                (SPSEQSignature) pp.getSpsEq().chgRep(spendResponseECDSA.getSignature(), R.uS.inv(), providerPublicKey.getPkSpsEq())
        );
    }

    private MessageBlock constructSpendCouponMessageBlock(BigInteger promotionId, ZnElement dsid, UUID basketId) {
        return new MessageBlock(
                new ByteArrayImplementation("spend".getBytes()),
                new ByteArrayImplementation(promotionId.toByteArray()),
                new ByteArrayImplementation(dsid.getUniqueByteRepresentation()),
                new ByteArrayImplementation(basketId.toString().getBytes())
        );
    }


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
    @Deprecated
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
        var dsid = token.getDoubleSpendingId();
        var vectorH = providerPublicKey.getH(this.pp, promotionParameters);
        var newPointsVector = RingElementVector.fromStream(newPoints.stream().map(e -> pp.getBg().getZn().createZnElement(e)));


        /* Compute pseudorandom values */
        // As in credit-earn, we use the PRF to make the algorithm deterministic
        var R = computeSpendDeductRandomness(userKeyPair.getSk(), token);

        // Prepare a new commitment (cPre0, cPre1) based on the pseudorandom values
        var exponents = new RingElementVector(R.tS, usk, R.dsidUserS, R.dsrndS, R.zS).concatenate(newPointsVector);
        var cPre0 = vectorH.innerProduct(exponents).pow(R.uS).compute();
        var cPre1 = pp.getG1Generator().pow(R.uS).compute();

        /* Enable double-spending-protection by forcing usk and esk becoming public in that case
           If token is used twice in two different transactions, the provider observes (c0,c1), (c0',c1') with gamma!=gamma'
           Hence, the provider can easily retrieve usk and esk (using the Schnorr-trick, computing (c0-c0')/(gamma-gamma') for usk, analogously for esk). */
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var gamma = Util.hashGammaOld(zp, dsid, tid, cPre0, cPre1, tid);
        var c = usk.mul(gamma).add(token.getDoubleSpendRandomness());

        /* Compute El-Gamal encryption of esk^*_usr using under secret key esk
           This allows the provider to decrypt usk^*_usr in case of double spending with the leaked esk.
           By additionally storing esk^*_prov, the provider can retrieve esk^* and thus iteratively decrypt the new esks. */

        /* Build non-interactive (Fiat-Shamir transformed) ZKP to ensure that the user follows the rules of the protocol */
        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey);
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        var witness = new SpendDeductZkpWitnessInput(usk, token.getZ(), R.zS, token.getT(), R.tS, R.uS, R.dsidUserS, token.getDoubleSpendRandomness(), R.dsrndS, token.getPoints(), newPointsVector);
        var commonInput = new SpendDeductZkpCommonInput(gamma, c, dsid, cPre0, cPre1, token.getCommitment0());
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        // Assemble request
        return new SpendRequest(dsid, proof, c, cPre0, cPre1, token.getCommitment0(), token.getSignature());
    }

    /**
     * React to a legitimate spend request to allow the user retrieving an updated token with the value decreased by k.
     * Returns additional data for double-spending protection.
     *
     * @param spendRequest        the user's request
     * @param promotionParameters specifying the promotion that the user wants to spend his points on (i.e. her claim)
     * @param providerKeyPair     keypair of the provider
     * @param tid                 transaction id, should be verified by the provider
     * @param spendDeductTree     the zero knowledge proof to verify for this promotion
     * @param userChoice          byte representation of the user choice,
     *                            influences the computation of challenge generator gamma and provider share esk_prov
     * @return tuple of response to send to the user and information required for double-spending protection
     */
    @Deprecated
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
        var gamma = Util.hashGammaOld(pp.getBg().getZn(), spendRequest.getDsid(), tid, spendRequest.getCPre0(), spendRequest.getCPre1(), userChoice);
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
                new DoubleSpendingTag(commonInput.c, gamma)
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
    @Deprecated
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
                R.dsidUserS.add(spendResponse.getEskProvStar()),
                R.dsrndS,
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
        ZnElement c = dsTag.getC();
        ZnElement cPrime = dsTagPrime.getC();
        ZnElement cDifference = c.sub(cPrime);

        ZnElement gamma = dsTag.getGamma();
        ZnElement gammaPrime = dsTagPrime.getGamma();
        ZnElement gammaDifference = gamma.sub(gammaPrime);

        ZnElement dsBlame = cDifference.div(gammaDifference);

        // computing public key of the user blamed of double-spending
        UserPublicKey upk = new UserPublicKey(pp.getW().pow(dsBlame));

        // assemble and return output
        return new UserInfo(upk, dsBlame);
    }


    /*
     * end of double-spending database interface to be used by provider
     */

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
                5,
                "SpendDeduct"
        ).stream().map(ringElement -> (ZnElement) ringElement).collect(Collectors.toList());
        return new SpendDeductRandomness(prv.get(0), prv.get(1), prv.get(2), prv.get(3), prv.get(4));
    }

    private MessageBlock constructRegistrationCouponMessageBlock(UserPublicKey userPublicKey, String userInfo) {
        return new MessageBlock(
                new ByteArrayImplementation(userPublicKey.getUniqueByteRepresentation()),
                new ByteArrayImplementation(userInfo.getBytes())
        );
    }

    public IncentivePublicParameters getPp() {
        return this.pp;
    }

    public String toString() {
        return "IncentiveSystem(pp=" + this.getPp() + ")";
    }
}

