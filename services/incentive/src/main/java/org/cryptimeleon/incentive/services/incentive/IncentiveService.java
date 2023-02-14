package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.client.dto.inc.*;
import org.cryptimeleon.incentive.client.dto.provider.*;
import org.cryptimeleon.incentive.crypto.IncentiveSystemRestorer;
import org.cryptimeleon.incentive.crypto.callback.IRegistrationCouponDBHandler;
import org.cryptimeleon.incentive.crypto.callback.IStorePublicKeyVerificationHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.promotion.ContextManager;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.sideeffect.SideEffect;
import org.cryptimeleon.incentive.services.incentive.api.RegistrationCouponJSON;
import org.cryptimeleon.incentive.services.incentive.error.BasketAlreadyPaidException;
import org.cryptimeleon.incentive.services.incentive.error.BasketNotPaidException;
import org.cryptimeleon.incentive.services.incentive.error.IncentiveServiceException;
import org.cryptimeleon.incentive.services.incentive.error.OnlineDoubleSpendingException;
import org.cryptimeleon.incentive.services.incentive.repository.*;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main service of the system that handles client requests for
 * joining an incentive system, earning points and spending tokens.
 * <p>
 * More precisely, this service runs the server side of the crypto protocols with clients
 * (i.e. Issue in Issue-Join, Credit in Credit-Earn and Deduct in Spend-Deduct).
 * <p>
 * Furthermore, this service also issues registration tokens.
 */
@Service
public class IncentiveService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IncentiveService.class);
    private final JSONConverter jsonConverter = new JSONConverter();
    private final CryptoRepository cryptoRepository;
    private final PromotionRepository promotionRepository;
    private final BasketRepository basketRepository;
    private final TokenUpdateResultRepository tokenUpdateResultRepository;
    private final DSPRepository offlineDspRepository;
    private final RegistrationCouponRepository registrationCouponRepository;
    private final TransactionRepository transactionRepository;
    private final DsidBlacklistRepository dsidBlacklistRepository;

    @Autowired
    private IncentiveService(CryptoRepository cryptoRepository,
                             PromotionRepository promotionRepository,
                             BasketRepository basketRepository,
                             TokenUpdateResultRepository tokenUpdateResultRepository,
                             DSPRepository offlineDspRepository,
                             RegistrationCouponRepository registrationCouponRepository,
                             TransactionRepository transactionRepository, DsidBlacklistRepository dsidBlacklistRepository) {
        this.cryptoRepository = cryptoRepository;
        this.promotionRepository = promotionRepository;
        this.basketRepository = basketRepository;
        this.tokenUpdateResultRepository = tokenUpdateResultRepository;
        this.offlineDspRepository = offlineDspRepository;
        this.registrationCouponRepository = registrationCouponRepository;
        this.transactionRepository = transactionRepository;
        this.dsidBlacklistRepository = dsidBlacklistRepository;
    }

    /**
     * Returns a list of all promotions in the system.
     *
     * @return array of strings (string representations of promotions)
     */
    public String[] getPromotions() {
        return promotionRepository.getPromotions().stream().map(RepresentableRepresentation::new).map(jsonConverter::serialize).toArray(String[]::new);
    }

    /**
     * Executes Issue algorithm of Issue-Join protocol for the passed promotion and join request
     * to let user join the promotion specified by the promotion ID
     * (Issue-Join yields a token for the respective promotion that contains no points).
     *
     * @param promotionId           the id that identifies the promotion
     * @param serializedJoinRequest the serialized join request
     * @return a serialized representation of a join response
     */
    public String joinPromotion(BigInteger promotionId, String serializedJoinRequest) {
        // find promotion by ID, throw exception if doesn't exist
        Promotion promotion = promotionRepository.getPromotion(promotionId).orElseThrow(() -> new IncentiveServiceException("Promotion to join not found!"));
        // retrieve public params, keys and incentive system instance
        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();
        // generate a join request
        JoinRequest joinRequest = new JoinRequest(jsonConverter.deserialize(serializedJoinRequest), pp, providerPublicKey);
        // run Issue algorithm to obtain a join response
        ProviderKeyPair providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        JoinResponse joinResponse = incentiveSystem.generateJoinRequestResponse(promotion.getPromotionParameters(), providerKeyPair, joinRequest);
        // compute and return serialized representation of join response
        return jsonConverter.serialize(joinResponse.getRepresentation());
    }

    /**
     * Executes Earn algorithm for the passed promotion, earn request and basket
     * and returns a signature for the updated token.
     * Communicates with basket server to ensure the request is valid.
     *
     * @param serializedEarnRequest the earn request to process
     * @param basketId              id of the basket that is used for this earn protocol run
     * @return serialized representation of SPS-EQ signature (= earn response)
     */
    private String handleEarnRequest(BigInteger promotionId, String serializedEarnRequest, UUID basketId) {
        log.info("EarnRequest:" + serializedEarnRequest);
        // find promotion by ID, throw exception if doesn't exist
        Promotion promotion = promotionRepository.getPromotion(promotionId).orElseThrow(() -> new IncentiveServiceException(String.format(Locale.getDefault(), "promotionId %d not found", promotionId)));
        // retrieve basket (ensure != null)
        Basket basket = basketRepository.getBasket(basketId);
        if (basket == null) throw new IncentiveServiceException("Basket not found!");
        log.info("Queried user basket " + basket);
        // TODO this basket api will change, how about storing a hash of the request only?
        // TODO sanity checks on basket, wait for new api
        // compute vector of points that user will earn for her basket
        Vector<BigInteger> pointsToEarn = promotion.computeEarningsForBasket(basket);
        // retrieve public params, keys and incentive system instance
        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();
        // run Credit algorithm to process earn request and compute+serialize earn response
        var earnRequest = new EarnRequest(jsonConverter.deserialize(serializedEarnRequest), pp);
        var providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        var signature = incentiveSystem.generateEarnRequestResponse(promotion.getPromotionParameters(), earnRequest, pointsToEarn, providerKeyPair);
        // compute and return serialized representation of earn response
        return jsonConverter.serialize(signature.getRepresentation());
    }

    /**
     * Processes a single spend request and returns a description of the side effect that occured during the transaction
     * (usually what reward the user chose or that she was caught double-spending).
     *
     * @param promotionId            identifier for the promotion the user issuing the spend request wants to take part in
     * @param basketId               identifier for the basket the user used
     * @param rewardId               identifier for the reward the user wants to claim with this spend transaction
     * @param serializedSpendRequest serialized representation of the spend request
     * @param serializedMetadata     serialized metadata of this request
     * @return side effect description (SideEffect object)
     */
    private SideEffect handleSpendRequest(BigInteger promotionId, UUID basketId, UUID rewardId, String serializedSpendRequest, String serializedMetadata) {
        log.info("SpendRequest:" + serializedSpendRequest);
        // find promotion by ID, throw exception if doesn't exist
        Promotion promotion = promotionRepository.getPromotion(promotionId).orElseThrow(() -> new IncentiveServiceException(String.format(Locale.getDefault(), "promotionId %d not found", promotionId)));
        ZkpTokenUpdate zkpTokenUpdate = promotion.getZkpTokenUpdates().stream().filter(reward1 -> reward1.getTokenUpdateId().equals(rewardId)).findAny().orElseThrow(() -> new IncentiveServiceException("Reward id not found"));
        // retrieve public params, keys and incentive system instance
        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();
        // retrieve basket (ensure != null)
        Basket basket = basketRepository.getBasket(basketId);
        if (basket == null) throw new IncentiveServiceException("Basket not found!");
        log.info("Queried user basket " + basket);
        // TODO some sanity checks on basket, wait for new basket service api
        // prepare zkp that proves that user is eligible for the intended spend transaction
        var metadata = (ZkpTokenUpdateMetadata) ((RepresentableRepresentation) jsonConverter.deserialize(serializedMetadata)).recreateRepresentable();
        if (!zkpTokenUpdate.validateTokenUpdateMetadata(metadata)) {
            throw new RuntimeException("Metadata is invalid for zkpTokenUpdate!");
        }
        // compute point vector that user earns for her basket
        var basketPoints = promotion.computeEarningsForBasket(basket);
        // generate tree that represents boolean formula that user token needs to fulfill for this spend transaction to work
        var spendDeductTree = zkpTokenUpdate.generateRelationTree(basketPoints, metadata);
        // transaction ID = basket ID
        var tid = basket.getBasketId(pp.getBg().getZn());
        // deserialize and restore spend request from representation
        FiatShamirProofSystem spendDeductProofSystem = new FiatShamirProofSystem(new SpendDeductBooleanZkp(spendDeductTree, pp, promotion.getPromotionParameters(), providerPublicKey));
        var spendRequest = new SpendRequest(jsonConverter.deserialize(serializedSpendRequest), pp, spendDeductProofSystem, tid, tid);
        /*
         * run Deduct
         * using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
         */
        DeductOutput deductOutput = incentiveSystem.generateSpendRequestResponse(promotion.getPromotionParameters(), spendRequest, new ProviderKeyPair(providerSecretKey, providerPublicKey), tid, spendDeductTree, tid // user choice
        );
        /*
         * Incentive service queries double-spending protection service for whether dsid of spent token is already contained.
         * If yes: abort transaction (since trivially identified as double-spending) and return dedicated caught-double-spending side effect.
         * Else: spend request is answered as normal, transaction is recorded into the database as soon as possible
         *
         * If dsp service is down at the time of the above query (due to a simulated dos attack), the transaction is recorded in the database regardless of the used dsid.
         *
         * Note that the above check is one-sided, i.e. while it never wrongly identifies a transaction as double-spending,
         * it cannot identify all invalid transactions.
         * An example scenario where the check does not notice an invalid transaction would be the following:
         * 1. Customer spends token t1 in store A, obtaining remainder token t1'.
         * 2. Store B is temporarily disconnected from the double-spending protection service
         * 3. Customer spends token t1 in store B, obtaining remainder token t1''.
         * 4. Customer spends token t1'' in store C.
         * 5. Store B reconnects to the dsprotection service and syncs transaction into database.
         *    => it was not known to dsprotection service (thus also to the incentive service) in step 4 that the spent token resulted from a double-spending transaction
         *    => transaction was recorded in step 4 despite it is invalid
         *    => needs to be invalidated now
         *
         * For the invalidation in step 5 to be possible,
         * it is critical that transactions that occured during dsp service downtime are always recorded in the database,
         * no matter whether dsid was already known.
         */
        Zn.ZnElement usedTokenDsid = spendRequest.getDsid();

        // TODO this has to be updated to new architecture!
        // E.g. by storing responses for dsids and returning the same response if a dsid is not sent the first time

        if (offlineDspRepository.containsDsid(usedTokenDsid)) {
            // immediately reject transaction if no simulated DoS attack ongoing and spent token already contained
            throw new OnlineDoubleSpendingException();
        }

        // compute and store serialized representation of the spend response
        var result = jsonConverter.serialize(deductOutput.getSpendResponse().getRepresentation());
        log.info("SpendResult: " + result);
        tokenUpdateResultRepository.insertZkpTokenUpdateResponse(basketId, promotionId, zkpTokenUpdate.getTokenUpdateId(), result);
        // return the side effect of the transaction ("what user actually achieved with it", i.e. got a frying pan, ...)
        return zkpTokenUpdate.getSideEffect();
    }

    /**
     * Adds promotions to the system (specified by a list of serialized representations).
     */
    public void addPromotions(List<String> serializedPromotions) {
        for (String serializedPromotion : serializedPromotions) {
            Promotion promotion = recreatePromotionFromRepresentation(serializedPromotion);
            promotionRepository.addPromotion(promotion);
        }
    }

    /**
     * Restores a promotion from its serialized representation.
     *
     * @param serializedPromotion serialized representation of a promotion
     * @return promotion object (see promotion package)
     */
    private Promotion recreatePromotionFromRepresentation(String serializedPromotion) {
        RepresentableRepresentation representableRepresentation = (RepresentableRepresentation) jsonConverter.deserialize(serializedPromotion);
        return (Promotion) representableRepresentation.recreateRepresentable();
    }

    /**
     * Clears all promotions from the system.
     */
    public void deleteAllPromotions() {
        promotionRepository.deleteAllPromotions();
    }

    /**
     * Processes a bulk of spend and earn requests that is specified by the passed data transfer object (DTO).
     *
     * @param basketId ID of the basket to apply the spends/earns to
     */
    public void handleBulk(UUID basketId, BulkRequestDto bulkRequestDto) {
        // can only perform zkp updates on baskets that are locked but not paid.
        basketRepository.lockBasket(basketId);
        if (basketRepository.isBasketPaid(basketId)) {
            throw new BasketAlreadyPaidException();
        }
        log.info("Start bulk proofs");
        /*
         * Initialize empty list of granted rewards.
         * Rewards are only granted to user after basket is paid, so they need to be saved for later.
         */
        var rewardIds = new ArrayList<String>();
        // process spend requests
        for (SpendRequestDto spendRequestDto : bulkRequestDto.getSpendRequestDtoList()) {
            /*
             * Handles spend request and synchronizes occured transaction into double-spending database.
             * Computes effect of spend transaction.
             */
            var sideEffect = handleSpendRequest(spendRequestDto.getPromotionId(), basketId, spendRequestDto.getTokenUpdateId(), spendRequestDto.getSerializedSpendRequest(), spendRequestDto.getSerializedMetadata());
            // if side effect is granting some reward: add respective reward ID to list
            if (sideEffect instanceof RewardSideEffect) {
                rewardIds.add(((RewardSideEffect) sideEffect).getRewardId());
            }
        }
        log.info("All spend requests processed.");
        // add rewards to basket
        basketRepository.setRewardsOfBasket(basketId, rewardIds);
        log.info("Added rewards to basket.");
        // handle earn requests
        for (EarnRequestDto earnRequestDto : bulkRequestDto.getEarnRequestDtoList()) {
            // handle a single earn request
            var result = handleEarnRequest(earnRequestDto.getPromotionId(), earnRequestDto.getSerializedEarnRequest(), basketId);
            log.info("EarnResult: " + result);
            // remember earn responses for later (earned points only granted after basket paid)
            tokenUpdateResultRepository.insertEarnResponse(basketId, earnRequestDto.getPromotionId(), result);
        }
        log.info("Processed earn requests.");
        log.info("Bulk proofs for basket " + basketId.toString() + " finished!");
    }

    /**
     * Obtain all earn responses that are currently unapplied for the basket identified by the passed basket ID.
     * If the specified basket is not paid, an exception occurs.
     *
     * @return DTO containing earn responses (= token updates)
     */
    public TokenUpdateResultsDto retrieveBulkResults(UUID basketId) {
        // can only retrieve updates if basket was already paid
        if (!basketRepository.isBasketPaid(basketId)) {
            throw new BasketNotPaidException();
        }
        // create and return DTO
        var results = tokenUpdateResultRepository.getUpdateResults(basketId).values();
        log.info(String.valueOf(results));
        return new TokenUpdateResultsDto(results.stream().filter(tokenUpdateResult -> tokenUpdateResult instanceof ZkpTokenUpdateResultDto).map(i -> (ZkpTokenUpdateResultDto) i).collect(Collectors.toList()), results.stream().filter(tokenUpdateResult -> tokenUpdateResult instanceof EarnTokenUpdateResultDto).map(i -> (EarnTokenUpdateResultDto) i).collect(Collectors.toList()));
    }

    public String registerUser(String serializedRegistrationCoupon) {
        var pp = cryptoRepository.getPublicParameters();
        var providerKeyPair = cryptoRepository.getProviderKeyPair();
        var registrationCoupon = new RegistrationCoupon(jsonConverter.deserialize(serializedRegistrationCoupon), new IncentiveSystemRestorer(pp));

        // Callbacks for crypto implementation.
        // TODO: Currently, we allow the message to be signed under any store public key
        // TODO: Do we need some kind of check whether users are already part of the system
        IStorePublicKeyVerificationHandler verificationHandler = (storePublicKey) -> true;
        IRegistrationCouponDBHandler registrationCouponDBHandler = registrationCouponRepository::addCoupon;

        var registrationToken = cryptoRepository.getIncentiveSystem().verifyRegistrationCouponAndIssueRegistrationToken(
                providerKeyPair,
                registrationCoupon,
                verificationHandler,
                registrationCouponDBHandler
        );

        return jsonConverter.serialize(registrationToken.getRepresentation());
    }

    public List<RegistrationCouponJSON> getRegistrationCoupons() {
        return registrationCouponRepository.getAllCoupons().stream().map((coupon) ->
                new RegistrationCouponJSON(
                        coupon.getUserInfo(),
                        jsonConverter.serialize(coupon.getUserPublicKey().getRepresentation()),
                        jsonConverter.serialize(coupon.getSignature().getRepresentation()),
                        jsonConverter.serialize(coupon.getStorePublicKey().getRepresentation())
                )
        ).collect(Collectors.toList());
    }

    public BulkResultsProviderDto bulk(BulkRequestProviderDto bulkRequestProviderDto) {
        var serializedEarnResults = bulkRequestProviderDto.getEarnRequests().stream()
                .map(this::earn)
                .collect(Collectors.toList());

        var serializedSpendResults = bulkRequestProviderDto.getSpendRequests().stream()
                .map(this::spend)
                .collect(Collectors.toList());

        return new BulkResultsProviderDto(serializedEarnResults, serializedSpendResults);
    }

    private EarnResultProviderDto earn(EarnRequestProviderDto earnRequestProviderDto) {
        var promotion = promotionRepository.getPromotion(earnRequestProviderDto.getPromotionId())
                .orElseThrow(() -> new IncentiveServiceException(String.format("Promotion with id %s not found!", earnRequestProviderDto.getPromotionId())));
        var earnRequestEcdsa = new EarnRequestECDSA(
                jsonConverter.deserialize(earnRequestProviderDto.getSerializedEarnRequestECDSA()),
                cryptoRepository.getPublicParameters()
        );
        var earnResult = cryptoRepository.getIncentiveSystem().generateEarnResponse(
                earnRequestEcdsa,
                promotion.getPromotionParameters(),
                cryptoRepository.getProviderKeyPair(),
                transactionRepository,
                storePublicKey -> true
        );
        return new EarnResultProviderDto(promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(earnResult.getRepresentation()));
    }

    private SpendResultsProviderDto spend(SpendRequestProviderDto spendRequestProviderDto) {
        var promotion = promotionRepository.getPromotion(spendRequestProviderDto.getPromotionId())
                .orElseThrow(() -> new IncentiveServiceException(String.format("Promotion with id %s not found!", spendRequestProviderDto.getPromotionId())));
        var tokenUpdate = promotion.getZkpTokenUpdates().stream()
                .filter(x -> x.getTokenUpdateId().equals(spendRequestProviderDto.getTokenUpdateId()))
                .findAny()
                .orElseThrow(() -> new IncentiveServiceException(String.format("Token update with id %s for promotion of id %s not found!", spendRequestProviderDto.getTokenUpdateId(), spendRequestProviderDto.getPromotionId())));
        ZkpTokenUpdateMetadata zkpTokenUpdateMetadata = (ZkpTokenUpdateMetadata) ((RepresentableRepresentation) jsonConverter.deserialize(spendRequestProviderDto.getSerializedTokenUpdateMetadata())).recreateRepresentable();
        Vector<BigInteger> basketPoints = new Vector<>(spendRequestProviderDto.getBasketPoints());
        var tree = tokenUpdate.generateRelationTree(basketPoints, zkpTokenUpdateMetadata);

        var context = ContextManager.computeContext(spendRequestProviderDto.getTokenUpdateId(), basketPoints, zkpTokenUpdateMetadata);
        var spendRequest = new SpendRequestECDSA(
                jsonConverter.deserialize(spendRequestProviderDto.getSerializedSpendRequest()),
                cryptoRepository.getPublicParameters(),
                promotion.getPromotionParameters(),
                spendRequestProviderDto.getBasketId(),
                tree,
                cryptoRepository.getProviderPublicKey(),
                context
        );
        var spendResult = cryptoRepository.getIncentiveSystem().verifySpendRequestAndIssueNewToken(
                cryptoRepository.getProviderKeyPair(),
                promotion.getPromotionParameters(),
                spendRequest,
                spendRequestProviderDto.getBasketId(),
                tree,
                context,
                s -> true,
                dsidBlacklistRepository
        );
        return new SpendResultsProviderDto(promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(spendResult.getRepresentation()));
    }
}
