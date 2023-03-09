package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.common.ByteArrayImplementation;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.ecdsa.ECDSASignature;
import org.cryptimeleon.craco.sig.ecdsa.ECDSASignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentive.crypto.callback.*;
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

    /*
     * Registration
     */

    public StoreKeyPair generateStoreKeyPair() {
        return Setup.storeKeyGen();
    }

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
     * @param registrationCoupon  the registration coupon to verify
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


    /*
     * implementation of the Issue {@literal <}-{@literal >}Join protocol
     */

    /**
     * Verify the registration token signature.
     *
     * @param providerPublicKey          the public key of the provider used for verification
     * @param registrationTokenSignature the registration token signature
     * @param userPublicKey              the user public key that is signed in this registration token
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

    /*
     * end of the implementation of the Issue {@literal <}-{@literal >}Join protocol
     */

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

        // assemble and return token
        Zn usedZn = pp.getBg().getZn();
        RingElementVector zeros = RingElementVector.generate(usedZn::getZeroElement, promotionParameters.getPointsVectorSize());

        return new Token(finalCommitment0, R.dsidUser.add(jRes.getDsidProv()), R.dsrnd, R.z, R.t, promotionParameters.getPromotionId(), zeros, finalCert);
    }

    /**
     * Generate the request to the store for getting an 'earn coupon' for a specific promotion and basket.
     *
     * @param token       earn points for this token
     * @param userKeyPair only required for pseudorandomness
     * @return a request that can be processed by the store
     */
    public EarnStoreRequest generateEarnCouponRequest(Token token, UserKeyPair userKeyPair) {
        // Compute pseudorandom value from the token that is used to blind the commitment
        // This makes this algorithm deterministic
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn");

        var c0Prime = token.getCommitment0().pow(s).compute();
        var c1Prime = pp.getG1Generator().pow(s).compute();
        var c2Prime = c1Prime.pow(token.getPromotionId()).compute();

        byte[] h = computeEarnHash(c0Prime, c1Prime, c2Prime);

        return new EarnStoreRequest(h);
    }

    /**
     * Method for the store to issue an earn coupon for a verified vector of points to earn for a fixed request and basket.
     * It needs to be verified that the basket is not yet redeemed for a request with different hash (we allow re-requesting
     * the same update in case of failure). Further, the basket needs to be marked as redeemed by this request.
     *
     * @param storeKeyPair               the keypair of the store required to issue the signature of the coupon
     * @param deltaK                     a verified amount of points the user is eligible to earn for the request
     * @param earnStoreRequest           the users request containing all necessary data for the store to verify the request and issue the coupon
     * @param storeBasketRedeemedHandler a callback to 1) Check that this basket can be redeemed with this request 2) Marks this basket as redeemed for this request
     * @return the 'coupon', which is essentially a signature on the earn update data under the stores ECDSA key
     */
    public EarnStoreResponse signEarnCoupon(StoreKeyPair storeKeyPair,
                                            Vector<BigInteger> deltaK,
                                            EarnStoreRequest earnStoreRequest,
                                            UUID basketId,
                                            BigInteger promotionId,
                                            IStoreBasketRedeemedHandler storeBasketRedeemedHandler) {
        IStoreBasketRedeemedHandler.BasketRedeemState issueSignature = storeBasketRedeemedHandler.verifyAndRedeemBasketEarn(basketId, promotionId, earnStoreRequest.getH());
        if (issueSignature.equals(IStoreBasketRedeemedHandler.BasketRedeemState.BASKET_REDEEMED_ABORT)) {
            throw new RuntimeException(String.format("Basket %s already redeemed with different hash than %s!", basketId.toString(), Arrays.toString(earnStoreRequest.getH())));
        }

        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        var message = constructEarnCouponMessageBlock(promotionId, deltaK, earnStoreRequest.getH());
        var signature = (ECDSASignature) ecdsaSignatureScheme.sign(message, storeKeyPair.getSk().getEcdsaSigningKey());

        return new EarnStoreResponse(signature, storeKeyPair.getPk());
    }

    /**
     * Verify that an earn store coupon is signed under a trusted public key.
     *
     * @param earnStoreRequest                  the requests which holds most data of the coupon
     * @param deltaK                            the earn-amount of the coupon
     * @param earnStoreResponse          the signature of the coupon including the verification key
     * @param storePublicKeyVerificationHandler a callback to verify that the verification key is trusted
     * @return true if all checks pass
     */
    public boolean verifyEarnCoupon(EarnStoreRequest earnStoreRequest,
                                    BigInteger promotionId,
                                    Vector<BigInteger> deltaK,
                                    EarnStoreResponse earnStoreResponse,
                                    IStorePublicKeyVerificationHandler storePublicKeyVerificationHandler) {

        // Verify Store ECDSA public key is trusted
        if (!storePublicKeyVerificationHandler.isStorePublicKeyTrusted(earnStoreResponse.getStorePublicKey())) {
            throw new RuntimeException("Store public key is not trusted");
        }

        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        var message = constructEarnCouponMessageBlock(promotionId, deltaK, earnStoreRequest.getH());
        return ecdsaSignatureScheme.verify(message, earnStoreResponse.getSignature(), earnStoreResponse.getStorePublicKey().getEcdsaVerificationKey());
    }

    /**
     * Generate a earn request that uses a earn coupon signed by a store as a proof that one is eligigble to earn points.
     *
     * @param token                    the token to update, must be the same as used for the coupon
     * @param providerPublicKey        the public key of the provider required for blinding the tokens signature
     * @param userKeyPair              the user keypair
     * @param deltaK                   the vector of points that shall be added to the tokens points. Must match the amount signed by the {@literal earnStoreCouponSignature}
     * @param earnStoreResponse the signature of a trusted store admitting this earn request
     * @return the request to send to the provider
     */
    public EarnProviderRequest generateEarnRequest(
            Token token,
            ProviderPublicKey providerPublicKey,
            UserKeyPair userKeyPair,
            Vector<BigInteger> deltaK,
            EarnStoreResponse earnStoreResponse
    ) {
        // Re-compute pseudorandom blinding value
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn");

        // Blind commitments and change representation of signature such that it is valid for blinded commitments
        // The blinded commitments and signature are sent to the provider
        return new EarnProviderRequest(
                deltaK,
                earnStoreResponse,
                (SPSEQSignature) pp.getSpsEq().chgRep(
                        token.getSignature(),
                        s,
                        providerPublicKey.getPkSpsEq()
                ),
                token.getCommitment0().pow(s).compute(),  // Compute for concurrent computation
                pp.getG1Generator().pow(s).compute()
        );
    }
    // TODO remove promotionId and basketId from all crypto-requests! Sent alongside such that services can lookup stuff

    /**
     * Providers logic for handling a earn request. Does the following:
     * 1. Verify request authenticated by trusted store
     * 2. Verify token valid
     * 3. Update token to new score
     * 4. Store clearing data to make store 'pay' for issuing the points
     *
     * @param earnProviderRequest                  the earn request
     * @param promotionParameters               the parameters associated to the promotion from the user's request. Can be identified
     *                                          by the promotionId in the request
     * @param providerKeyPair                   the keys of the provider
     * @param transactionDBHandler              a callback for adding all relevant data to the clearing db
     * @param storePublicKeyVerificationHandler a callback for verifying that the store's key used to authenticate the request is trusted
     * @return the blindly updated signature
     */
    public SPSEQSignature generateEarnResponse(EarnProviderRequest earnProviderRequest,
                                               PromotionParameters promotionParameters,
                                               ProviderKeyPair providerKeyPair,
                                               IEarnTransactionDBHandler transactionDBHandler,
                                               IStorePublicKeyVerificationHandler storePublicKeyVerificationHandler) {
        // Blinded token
        var c0Prime = earnProviderRequest.getcPrime0();
        var c1Prime = earnProviderRequest.getcPrime1();
        var c2Prime = earnProviderRequest.getcPrime1().pow(promotionParameters.getPromotionId());

        // Compute hash h
        var h = computeEarnHash(c0Prime, c1Prime, c2Prime);

        // Verify Store ECDSA public key is trusted
        if (!storePublicKeyVerificationHandler.isStorePublicKeyTrusted(earnProviderRequest.getEarnStoreCoupon().getStorePublicKey())) {
            throw new RuntimeException("Store public key is not trusted");
        }

        // Verify ECDSA
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        var message = constructEarnCouponMessageBlock(promotionParameters.getPromotionId(), earnProviderRequest.getDeltaK(), h);
        var ecdsaValid = ecdsaSignatureScheme.verify(message, earnProviderRequest.getEarnStoreCoupon().getSignature(), earnProviderRequest.getEarnStoreCoupon().getStorePublicKey().getEcdsaVerificationKey());
        if (!ecdsaValid) throw new RuntimeException("ECDSA signature invalid");

        // Verify blinded SPSEQ
        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();
        var blindedSpseqValid = spseqSignatureScheme.verify(providerKeyPair.getPk().getPkSpsEq(), earnProviderRequest.getSpseqSignature(), GroupElementVector.of(c0Prime, c1Prime, c2Prime));
        if (!blindedSpseqValid) throw new RuntimeException("(Blinded) SPSEQ signature invalid");

        // Add to clearing DB
        var earnTxData = new EarnTransactionData(earnProviderRequest, h);
        transactionDBHandler.addEarnData(earnTxData);

        // Blind-sign update
        var Q = providerKeyPair.getSk().getTokenPointsQ(promotionParameters);
        var K = earnProviderRequest.getDeltaK().map(k -> pp.getBg().getG1().getZn().createZnElement(k));
        var c0PrimePlusDeltaK = c0Prime.op(c1Prime.pow(Q.innerProduct(K))).compute();

        return (SPSEQSignature) spseqSignatureScheme.sign(providerKeyPair.getSk().getSkSpsEq(), c0PrimePlusDeltaK, c1Prime, c2Prime);
    }

    /**
     * Obtains the updated token with more points from the blinded updated signature retrieved from the provider.
     *
     * @param earnRequest         the request sent to the provider
     * @param changedSignature    the response signature
     * @param promotionParameters the parameters of this promotion
     * @param token               the old token
     * @param userKeyPair         the user keypair
     * @param providerPublicKey   the provider's public key
     * @return the updated token with more points
     */
    public Token handleEarnResponse(EarnProviderRequest earnRequest,
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
                token.getDoubleSpendingId(),
                token.getDoubleSpendRandomness(),
                token.getZ(),
                token.getT(),
                token.getPromotionId(),
                new RingElementVector(token.getPoints().zip(K, RingElement::add)),
                (SPSEQSignature) newSignature
        );
    }

    /*
     * implementation of the Deduct {@literal <}-{@literal >}Spend protocol
     */

    /**
     * Generate a request to spend points of the token such that the remaining points are equal to {@literal newPoints}
     * and the update adheres to the {@literal spendDeductTree}.
     *
     * @param userKeyPair         the user key pair
     * @param providerPublicKey   the public key of the provider
     * @param token               the token to spend
     * @param promotionParameters the parameters of the promotion this token and update belong to
     * @param basketId            the id of the basket this operation belongs to
     * @param newPoints           the points the token will have after this operation
     * @param spendDeductTree     a boolean formula represented by a tree that must be satisfied
     * @param context             information that uniquely identify this updates parameters/rules.
     * @return the request to send to the store
     */
    public SpendStoreRequest generateStoreSpendRequest(UserKeyPair userKeyPair,
                                                       ProviderPublicKey providerPublicKey,
                                                       Token token,
                                                       PromotionParameters promotionParameters,
                                                       UUID basketId,
                                                       Vector<BigInteger> newPoints,
                                                       SpendDeductTree spendDeductTree,
                                                       UniqueByteRepresentable context) {
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

        var gamma = Util.hashGamma(zp, token.getDoubleSpendingId(), basketId, cPre0, cPre1, cPre2, context);
        var c = usk.mul(gamma).add(token.getDoubleSpendRandomness());

        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey);
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        var witness = new SpendDeductZkpWitnessInput(usk, token.getZ(), R.zS, token.getT(), R.tS, R.uS, R.dsidUserS, token.getDoubleSpendRandomness(), R.dsrndS, token.getPoints(), newPointsVector);
        var commonInput = new SpendDeductZkpCommonInput(gamma, c, token.getDoubleSpendingId(), cPre0, cPre1, token.getCommitment0());
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        return new SpendStoreRequest(token.getDoubleSpendingId(), c, token.getSignature(), token.getCommitment0(), cPre0, cPre1, proof);
    }

    /**
     * Verify a spend request for a basket and issue a ECDSA signature to authorize the request at the provider.
     * After that, do the following steps before giving the signature to users:
     * 1. Wait for payment
     * 2. Issue reward
     *
     * @param storeKeyPair          the store key pair
     * @param providerPublicKey     the public key of the provider
     * @param basketId              the id of the basket this request belongs to
     * @param promotionParameters   the parameters that are associated to the promotions of this request
     * @param spendStoreRequest    the request of the user
     * @param spendDeductTree       a boolean formula that must be satisfied by the user's request
     * @param context               information that uniquely identify this updates parameters/rules.
     * @param basketRedeemedHandler some instance that provides functionality for associating requests to baskets,
     *                              checking if the basket was already redeemed, etc.
     * @param dsidBlacklistHandler  an instance that provides functionality for blacklisting dsids and allowing retries
     * @param transactionDBHandler  an instance for functionality for storing this transactions data
     * @return the ECDSA signature
     */
    public SpendStoreResponse signSpendCoupon(StoreKeyPair storeKeyPair,
                                              ProviderPublicKey providerPublicKey,
                                              UUID basketId,
                                              PromotionParameters promotionParameters,
                                              SpendStoreRequest spendStoreRequest,
                                              SpendDeductTree spendDeductTree,
                                              UniqueByteRepresentable context,
                                              IStoreBasketRedeemedHandler basketRedeemedHandler,
                                              IDsidBlacklistHandler dsidBlacklistHandler,
                                              ISpendTransactionDBHandler transactionDBHandler
    ) {
        var zp = pp.getBg().getZn();

        // Verify old token signature valid
        var c0 = spendStoreRequest.getC0();
        var c1 = pp.getG1Generator();
        var c2 = c1.pow(promotionParameters.getPromotionId());

        var spseqValid = pp.getSpsEq().verify(providerPublicKey.getPkSpsEq(), spendStoreRequest.getSigma(), c0, c1, c2);
        if (!spseqValid) {
            throw new RuntimeException("Invalid token signature");
        }

        // Compute gamma
        var cPre0 = spendStoreRequest.getCPre0();
        var cPre1 = spendStoreRequest.getCPre1();
        var cPre2 = cPre1.pow(promotionParameters.getPromotionId()).compute();

        var gamma = Util.hashGamma(zp, spendStoreRequest.getDsid(), basketId, cPre0, cPre1, cPre2, context); // TODO include all user choices

        // Verify proof
        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey);
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        var commonInput = new SpendDeductZkpCommonInput(spendStoreRequest, gamma);
        var proofValid = fiatShamirProofSystem.checkProof(commonInput, spendStoreRequest.getSpendZkp());
        if (!proofValid) {
            throw new IllegalArgumentException("ZKP of the request is not valid!");
        }

        // Signature to legitimate retrieving a new token
        var ecdsa = new ECDSASignatureScheme();
        MessageBlock spendCouponMessageBlock = constructSpendCouponMessageBlock(promotionParameters.getPromotionId(), spendStoreRequest.getDsid(), basketId);
        var signature = (ECDSASignature) ecdsa.sign(spendCouponMessageBlock, storeKeyPair.getSk().getEcdsaSigningKey());
        var spendCouponSignature = new SpendStoreResponse(signature, storeKeyPair.getPk());

        // Check if basket and request qualify this request
        var redeemResult = basketRedeemedHandler.verifyAndRedeemBasketSpend(basketId, promotionParameters.getPromotionId(), gamma);
        switch (redeemResult) {
            case BASKET_NOT_REDEEMED:
                if (dsidBlacklistHandler.containsDsidWithDifferentGamma(commonInput.dsid, gamma)) {
                    throw new RuntimeException("Token with dsid already spent with different basket!");
                }
                break;
            case BASKET_REDEEMED_ABORT:
                throw new RuntimeException("Basket already redeemed for different request!");
            case BASKED_REDEEMED_RETRY:
                // Retry, just perform the protocol again, no need to add it to dsid blacklist
                break;
        }

        // Blacklist dsid at provider and send clearing data => Provider finds users that perform double-spending attack!
        var spendClearingData = new SpendTransactionData(spendStoreRequest, promotionParameters.getPromotionId(), basketId, signature, storeKeyPair.getPk(), gamma);
        transactionDBHandler.addSpendData(spendClearingData);

        return spendCouponSignature;
    }

    /**
     * Verify the spend coupon signature.
     * Does not verify the store's public key!
     *
     * @param spendStoreRequest   the request sent to obtain the signature
     * @param spendCouponSignature the signature + public key
     * @param promotionParameters  the parameters of the promotion this signature belongs to
     * @param basketId             the id the corresponding basket
     * @return whether the signature is valid
     */
    public boolean verifySpendCouponSignature(SpendStoreRequest spendStoreRequest, SpendStoreResponse spendCouponSignature, PromotionParameters promotionParameters, UUID basketId) {
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        MessageBlock messageBlock = constructSpendCouponMessageBlock(promotionParameters.getPromotionId(), spendStoreRequest.getDsid(), basketId);
        return ecdsaSignatureScheme.verify(messageBlock, spendCouponSignature.getSignature(), spendCouponSignature.getStorePublicKey().getEcdsaVerificationKey());
    }

    /**
     * Verify a spend request at provider side and issue a new token.
     * Checks if the request was authorized by a trusted store, and ensures only one remainder token is issued
     * (assuming the dsidBlacklistHandler has consistent data at all time).
     *
     * @param providerKeyPair                   the key pair of the provider
     * @param promotionParameters               the parameter of the promotion this request belongs to
     * @param spendProviderRequest                 the spend request sent by the user
     * @param spendDeductTree                   a boolean formula that must be satisfied by the user's request
     * @param context                           information that uniquely identify this updates parameters/rules.
     * @param storePublicKeyVerificationHandler an instance that provides functionality for verifying a store pulic key
     * @param dsidBlacklistHandler              an instance that provides functionality for dsid blacklisting and retrying
     * @return a response containing a SPSEQ signature and the provider's part of the new dsid
     */
    public SpendProviderResponse verifySpendRequestAndIssueNewToken(ProviderKeyPair providerKeyPair,
                                                                    PromotionParameters promotionParameters,
                                                                    SpendProviderRequest spendProviderRequest,
                                                                    UUID basketId,
                                                                    SpendDeductTree spendDeductTree,
                                                                    UniqueByteRepresentable context,
                                                                    IStorePublicKeyVerificationHandler storePublicKeyVerificationHandler,
                                                                    IDsidBlacklistHandler dsidBlacklistHandler) {

        // 0. Check if this is a doublespending attempt.
        var gamma = Util.hashGamma(pp.getBg().getZn(),
                spendProviderRequest.getDoubleSpendingId(),
                basketId,
                spendProviderRequest.getcPre0(),
                spendProviderRequest.getcPre1(),
                spendProviderRequest.getcPre1().pow(promotionParameters.getPromotionId()),
                context);
        if (dsidBlacklistHandler.containsDsidWithDifferentGamma(spendProviderRequest.getDoubleSpendingId(), gamma)) {
            throw new RuntimeException("Illegal retry, dsid already used for different request!");
        }

        // 1. Verify Store ECDSA public key is trusted
        if (!storePublicKeyVerificationHandler.isStorePublicKeyTrusted(spendProviderRequest.getStorePublicKey())) {
            throw new RuntimeException("Store public key is not trusted");
        }

        // 2. Verify ECDSA
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        MessageBlock messageBlock = constructSpendCouponMessageBlock(promotionParameters.getPromotionId(), spendProviderRequest.getDoubleSpendingId(), basketId);
        boolean ecdsaValid = ecdsaSignatureScheme.verify(messageBlock, spendProviderRequest.getCouponSignature(), spendProviderRequest.getStorePublicKey().getEcdsaVerificationKey());
        if (!ecdsaValid) {
            throw new RuntimeException("Invalid ECDSA signature!");
        }

        // 3. Verify SPSEQ
        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();
        spseqSignatureScheme.verify(providerKeyPair.getPk().getPkSpsEq(), spendProviderRequest.getTokenSignature(), spendProviderRequest.getC0(), pp.getG1Generator(), pp.getG1Generator().pow(promotionParameters.getPromotionId()));

        // 4. Verify NZIK
        var spendDeductZkp = new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerKeyPair.getPk());
        var fiatShamirProofSystem = new FiatShamirProofSystem(spendDeductZkp);
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        var commonInput = new SpendDeductZkpCommonInput(spendProviderRequest, gamma);
        var proofValid = fiatShamirProofSystem.checkProof(commonInput, spendProviderRequest.getProof());
        if (!proofValid) {
            throw new IllegalArgumentException("ZKP of the request is not valid!");
        }

        // 5. dsid_prov^*=prf('dsid', gamma)
        var preimage = new ByteArrayAccumulator();
        preimage.escapeAndSeparate(gamma);
        var dsidStarProv = pp.getPrfToZn().hashThenPrfToZn(providerKeyPair.getSk().getBetaProv(), new ByteArrayImplementation(preimage.extractBytes()), "dsid");

        // 6. Create signature for new token
        GroupElement cPre0 = spendProviderRequest.getcPre0();
        GroupElement cPre1 = spendProviderRequest.getcPre1();
        GroupElement cPre2 = cPre1.pow(promotionParameters.getPromotionId());
        SPSEQSignature updatedTokenSignature = (SPSEQSignature) spseqSignatureScheme.sign(
                providerKeyPair.getSk().getSkSpsEq(),
                cPre0.op(cPre1.pow(dsidStarProv.mul(providerKeyPair.getSk().getQ().get(1)))),
                cPre1,
                cPre2
        );

        // 7. Add to DoubleSpending DB
        dsidBlacklistHandler.addEntryIfDsidNotPresent(spendProviderRequest.getDoubleSpendingId(), gamma);

        return new SpendProviderResponse(updatedTokenSignature, dsidStarProv);
    }

    /**
     * Process a providers spend response to obtain the new token
     *
     * @param userKeyPair         the keypair of the user
     * @param providerPublicKey   the public key of the provider
     * @param token               the old token
     * @param promotionParameters the parameters of the promotion this token belongs to
     * @param newPoints           the point vector the new token should have, must satisfy the constraints of the update
     * @param spendProviderRequest   the request sent to the provider
     * @param spendProviderResponse  the response sent by the provider
     * @return a new token with new dsid
     */
    public Token retrieveUpdatedTokenFromSpendResponse(UserKeyPair userKeyPair,
                                                       ProviderPublicKey providerPublicKey,
                                                       Token token,
                                                       PromotionParameters promotionParameters,
                                                       Vector<BigInteger> newPoints,
                                                       SpendProviderRequest spendProviderRequest,
                                                       SpendProviderResponse spendProviderResponse) {
        var newPointsVector = RingElementVector.fromStream(newPoints.stream().map(e -> pp.getBg().getZn().createZnElement(e)));

        // Re-compute pseudorandom values
        var R = computeSpendDeductRandomness(userKeyPair.getSk(), token);

        // Verify the signature on the new, blinded commitment
        var blindedCStar0 = spendProviderRequest.getcPre0().op(providerPublicKey.getH().get(1).pow(spendProviderResponse.getDsidStarProv().mul(R.uS)));
        var blindedCStar1 = pp.getG1Generator().pow(R.uS);
        var blindedCStar2 = blindedCStar1.pow(promotionParameters.getPromotionId());
        var valid = pp.getSpsEq().verify(
                providerPublicKey.getPkSpsEq(),
                spendProviderResponse.getSignature(),
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
                R.dsidUserS.add(spendProviderResponse.getDsidStarProv()),
                R.dsrndS,
                R.zS,
                R.tS,
                token.getPromotionId(),
                new RingElementVector(newPointsVector),
                (SPSEQSignature) pp.getSpsEq().chgRep(spendProviderResponse.getSignature(), R.uS.inv(), providerPublicKey.getPkSpsEq())
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

