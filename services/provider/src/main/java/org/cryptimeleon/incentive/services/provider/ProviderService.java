package org.cryptimeleon.incentive.services.provider;

import org.cryptimeleon.incentive.client.dto.EnrichedSpendTransactionDataDto;
import org.cryptimeleon.incentive.client.dto.provider.*;
import org.cryptimeleon.incentive.crypto.IncentiveSystemRestorer;
import org.cryptimeleon.incentive.crypto.callback.IRegistrationCouponDBHandler;
import org.cryptimeleon.incentive.crypto.callback.IStorePublicKeyVerificationHandler;
import org.cryptimeleon.incentive.crypto.exception.ProviderDoubleSpendingDetectedException;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.promotion.ContextManager;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.services.provider.api.DSDetectedEntryDto;
import org.cryptimeleon.incentive.services.provider.api.RegistrationCouponJSON;
import org.cryptimeleon.incentive.services.provider.error.IncentiveServiceException;
import org.cryptimeleon.incentive.services.provider.error.OnlineDoubleSpendingException;
import org.cryptimeleon.incentive.services.provider.repository.*;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
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
public class ProviderService {
    private final JSONConverter jsonConverter = new JSONConverter();
    private final CryptoRepository cryptoRepository;
    private final PromotionRepository promotionRepository;
    private final RegistrationCouponRepository registrationCouponRepository;
    private final TransactionRepository transactionRepository;
    private final DsidBlacklistRepository dsidBlacklistRepository;

    @Autowired
    private ProviderService(CryptoRepository cryptoRepository,
                            PromotionRepository promotionRepository,
                            RegistrationCouponRepository registrationCouponRepository,
                            TransactionRepository transactionRepository, DsidBlacklistRepository dsidBlacklistRepository) {
        this.cryptoRepository = cryptoRepository;
        this.promotionRepository = promotionRepository;
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
        var earnRequestEcdsa = new EarnProviderRequest(
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

    private SpendResultProviderDto spend(SpendRequestProviderDto spendRequestProviderDto) {
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
        var spendRequest = new SpendProviderRequest(
                jsonConverter.deserialize(spendRequestProviderDto.getSerializedSpendRequest()),
                cryptoRepository.getPublicParameters(),
                promotion.getPromotionParameters(),
                spendRequestProviderDto.getBasketId(),
                tree,
                cryptoRepository.getProviderPublicKey(),
                context
        );
        SpendProviderResponse spendResult;
        try {
            spendResult = cryptoRepository.getIncentiveSystem().verifySpendRequestAndIssueNewToken(
                    cryptoRepository.getProviderKeyPair(),
                    promotion.getPromotionParameters(),
                    spendRequest,
                    spendRequestProviderDto.getBasketId(),
                    tree,
                    context,
                    s -> true,
                    dsidBlacklistRepository
            );
        } catch (ProviderDoubleSpendingDetectedException e) {
            throw new OnlineDoubleSpendingException();
        }
        return new SpendResultProviderDto(promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(spendResult.getRepresentation()));
    }

    public List<UUID> txDataBaskets() {
        return transactionRepository.getSpendTransactionDataList().values().stream()
                .flatMap(list -> list.stream().map(SpendTransactionData::getBasketId))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<DSDetectedEntryDto> doubleSpendingDetected() {
        return transactionRepository.getDoubleSpendingDetected().entrySet().stream().map(
                tuple -> {
                    var registrationCoupon = registrationCouponRepository.findEntryFor(tuple.getValue().getUpk()).orElseThrow();
                    return new DSDetectedEntryDto(tuple.getKey(), tuple.getValue(), registrationCoupon);
                }).collect(Collectors.toList());
    }

    public void addSpendTransactionData(EnrichedSpendTransactionDataDto enrichedSpendTransactionDataDto) {
        var serializedSpendData = enrichedSpendTransactionDataDto.getSerializedSpendTransactionData();
        var tokenUpdateId = enrichedSpendTransactionDataDto.getTokenUpdateId();
        var promotionId = enrichedSpendTransactionDataDto.getPromotionId();
        var serializedMetadata = enrichedSpendTransactionDataDto.getSerializedMetadata();
        var basketPoints = enrichedSpendTransactionDataDto.getBasketPoints();

        var promotion = promotionRepository.getPromotion(promotionId)
                .orElseThrow(() -> new IncentiveServiceException(String.format("Promotion with id %s not found!", enrichedSpendTransactionDataDto.getPromotionId())));
        var tokenUpdate = promotion.getZkpTokenUpdates().stream()
                .filter(x -> x.getTokenUpdateId().equals(enrichedSpendTransactionDataDto.getTokenUpdateId()))
                .findAny()
                .orElseThrow(() -> new IncentiveServiceException(String.format("Token update with id %s for promotion of id %s not found!", tokenUpdateId, promotionId)));
        ZkpTokenUpdateMetadata zkpTokenUpdateMetadata = (ZkpTokenUpdateMetadata) ((RepresentableRepresentation) jsonConverter.deserialize(serializedMetadata)).recreateRepresentable();
        Vector<BigInteger> basketPointVector = new Vector<>(basketPoints);
        var tree = tokenUpdate.generateRelationTree(basketPointVector, zkpTokenUpdateMetadata);

        var context = ContextManager.computeContext(tokenUpdateId, basketPointVector, zkpTokenUpdateMetadata);

        var spendData = new SpendTransactionData(jsonConverter.deserialize(serializedSpendData), cryptoRepository.getPublicParameters(), promotion.getPromotionParameters(), tree, cryptoRepository.getProviderPublicKey(), context);
        transactionRepository.addSpendData(spendData);
    }
}
