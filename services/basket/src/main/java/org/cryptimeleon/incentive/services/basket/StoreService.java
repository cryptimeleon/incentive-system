package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.client.dto.store.*;
import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.ContextManager;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.services.basket.repository.*;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;
import org.cryptimeleon.incentive.services.basket.storage.BasketRepository;
import org.cryptimeleon.incentive.services.basket.storage.RewardItemEntity;
import org.cryptimeleon.incentive.services.basket.storage.RewardItemRepository;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final CryptoRepository cryptoRepository;
    private final PromotionRepository promotionRepository;
    private final JSONConverter jsonConverter = new JSONConverter();
    private final BasketRepository basketRepository;
    private final DsidBlacklistRepository dsidBlacklistRepository;
    private final TransactionRepository transactionRepository;
    private final BulkResponseRepository bulkResponseRepository;
    private final RewardItemRepository rewardItemRepository;

    @Autowired
    private StoreService(CryptoRepository cryptoRepository,
                         PromotionRepository promotionRepository,
                         BasketRepository basketRepository,
                         DsidBlacklistRepository dsidBlacklistRepository,
                         TransactionRepository transactionRepository,
                         BulkResponseRepository bulkResponseRepository,
                         RewardItemRepository rewardItemRepository) {
        this.cryptoRepository = cryptoRepository;
        this.promotionRepository = promotionRepository;
        this.basketRepository = basketRepository;
        this.dsidBlacklistRepository = dsidBlacklistRepository;
        this.transactionRepository = transactionRepository;
        this.bulkResponseRepository = bulkResponseRepository;
        this.rewardItemRepository = rewardItemRepository;
    }

    public static Basket promotionBasketFromBasketEntity(BasketEntity basketEntity) {
        return new Basket(
                basketEntity.getBasketID(),
                basketEntity.getBasketItems().stream().map(itemInBasketEntity ->
                        new BasketItem(
                                itemInBasketEntity.getItem().getId(),
                                itemInBasketEntity.getItem().getTitle(),
                                Math.toIntExact(itemInBasketEntity.getItem().getPrice()),
                                itemInBasketEntity.getCount()
                        )
                ).collect(Collectors.toList())
        );
    }

    public String registerUserAndReturnSerializedRegistrationCoupon(String serializedUserPublicKey, String userInfo) {
        UserPublicKey userPublicKey = new UserPublicKey(jsonConverter.deserialize(serializedUserPublicKey), cryptoRepository.getPublicParameters());
        RegistrationCoupon registrationCoupon = cryptoRepository.getIncentiveSystem().signRegistrationCoupon(cryptoRepository.getStoreKeyPair(), userPublicKey, userInfo);
        return jsonConverter.serialize(registrationCoupon.getRepresentation());
    }

    public void processBulkRequest(BulkRequestStoreDto bulkRequestStoreDto) {
        // Verification
        UUID basketId = bulkRequestStoreDto.getBasketId();
        BasketEntity basketEntity = basketRepository.findById(basketId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find basket with id %s", basketId)));

        // Lock basket
        basketEntity.setLocked(true);
        basketRepository.save(basketEntity);

        Basket basket = promotionBasketFromBasketEntity(basketEntity);
        // Process all earn and spend requests and store results(TODO in parallel?, check if duplicate promotionIds?)
        // Earn
        List<EarnResultStoreDto> earnResultStoreDtoList = bulkRequestStoreDto.getEarnRequestStoreDtoList().stream().map(earnRequestStoreDto -> {
            EarnStoreResponse signature = earn(earnRequestStoreDto, basket);
            return new EarnResultStoreDto(earnRequestStoreDto.getPromotionId(), jsonConverter.serialize(signature.getRepresentation()));
        }).collect(Collectors.toList());

        // Spend
        List<SpendResultsStoreDto> spendResultsStoreDtoList = bulkRequestStoreDto.getSpendRequestStoreDtoList().stream().map(spendRequestStoreDto -> {
                    var signature = spend(spendRequestStoreDto, basket);
                    return new SpendResultsStoreDto(spendRequestStoreDto.getPromotionId(), jsonConverter.serialize(signature.getRepresentation()));
                }
        ).collect(Collectors.toList());

        bulkResponseRepository.addBulkResult(basketId, new BulkResultsStoreDto(earnResultStoreDtoList, spendResultsStoreDtoList));

        // Add reward items to basket
        List<RewardItemEntity> rewardItemEntities = bulkRequestStoreDto.getSpendRequestStoreDtoList().stream()
                .map(spendRequestStoreDto -> {
                    Promotion promotion = promotionRepository.getPromotion(spendRequestStoreDto.getPromotionId())
                            .orElseThrow(() -> new StoreException(String.format("Cannot find promotion with id %s", spendRequestStoreDto.getPromotionId())));
                    ZkpTokenUpdate requestedTokenUpdate = promotion.getZkpTokenUpdates().stream().filter(zkpTokenUpdate -> zkpTokenUpdate.getTokenUpdateId().equals(spendRequestStoreDto.getTokenUpdateId())).findAny()
                            .orElseThrow(() -> new StoreException(String.format("Cannot find token update with id %s in promotion with id %s", spendRequestStoreDto.getTokenUpdateId(), promotion)));
                    return requestedTokenUpdate.getSideEffect();
                })
                .filter(sideEffect -> sideEffect instanceof RewardSideEffect)
                .map(sideEffect -> (RewardSideEffect) sideEffect)
                .map(rewardSideEffect -> rewardItemRepository.findById(rewardSideEffect.getRewardId()))
                .filter(Optional::isPresent) // TODO or throw if not found?
                .map(Optional::get)
                .collect(Collectors.toList());

        basketEntity.addRewardItems(rewardItemEntities);
        basketRepository.save(basketEntity);
    }

    public BulkResultsStoreDto bulkResponses(UUID basketId) {
        // Verification
        BasketEntity basketEntity = basketRepository.findById(basketId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find basket with id %s", basketId)));

        if (!basketEntity.isPaid()) {
            throw new StoreException("This basket is not paid!");
        }

        // Return results
        return bulkResponseRepository.removeBulkResultFor(basketId);
    }

    public EarnStoreResponse earn(EarnRequestStoreDto earnRequestStoreDto, Basket basket) {
        var promotionId = earnRequestStoreDto.getPromotionId();
        EarnStoreRequest earnStoreRequest = new EarnStoreRequest(jsonConverter.deserialize(earnRequestStoreDto.getSerializedRequest()));
        Promotion promotion = promotionRepository.getPromotion(promotionId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find promotion with id %s", promotionId)));

        Vector<BigInteger> deltaK = promotion.computeEarningsForBasket(basket);

        return cryptoRepository.getIncentiveSystem().signEarnCoupon(
                cryptoRepository.getStoreKeyPair(),
                deltaK,
                earnStoreRequest,
                basket.getBasketId(),
                promotionId,
                this::checkBasketStateAndRedeem
        );
    }

    public SpendStoreResponse spend(SpendRequestStoreDto spendRequestStoreDto, Basket basket) {
        var serializedSpendStoreRequest = spendRequestStoreDto.getSerializedRequest();
        var promotionId = spendRequestStoreDto.getPromotionId();
        UUID tokenUpdateId = spendRequestStoreDto.getTokenUpdateId();
        String serializedZkpTokenUpdateMetadata = spendRequestStoreDto.getSerializedTokenUpdateMetadata();


        Promotion promotion = promotionRepository.getPromotion(promotionId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find promotion with id %s", promotionId)));
        ZkpTokenUpdateMetadata zkpTokenUpdateMetadata = (ZkpTokenUpdateMetadata) ((RepresentableRepresentation) jsonConverter.deserialize(serializedZkpTokenUpdateMetadata)).recreateRepresentable();

        ZkpTokenUpdate requestedTokenUpdate = promotion.getZkpTokenUpdates().stream().filter(zkpTokenUpdate -> zkpTokenUpdate.getTokenUpdateId().equals(tokenUpdateId)).findAny()
                .orElseThrow(() -> new StoreException(String.format("Cannot find token update with id %s in promotion with id %s", tokenUpdateId, promotion)));

        // Include value of basket into spend, i.e. users need to spend fewer points from the token if their basket is worth some
        Vector<BigInteger> basketValueForUpdate = promotion.computeEarningsForBasket(basket);
        SpendDeductTree relationTree = requestedTokenUpdate.generateRelationTree(basketValueForUpdate, zkpTokenUpdateMetadata);
        UniqueByteRepresentable context = ContextManager.computeContext(tokenUpdateId, basketValueForUpdate, zkpTokenUpdateMetadata);

        SpendStoreRequest spendStoreRequest = new SpendStoreRequest(jsonConverter.deserialize(serializedSpendStoreRequest),
                cryptoRepository.getPublicParameters(),
                basket.getBasketId(),
                promotion.getPromotionParameters(),
                cryptoRepository.getProviderPublicKey(),
                relationTree,
                context);

        return cryptoRepository.getIncentiveSystem().signSpendCoupon(
                cryptoRepository.getStoreKeyPair(),
                cryptoRepository.getProviderPublicKey(),
                basket.getBasketId(),
                promotion.getPromotionParameters(),
                spendStoreRequest,
                relationTree,
                context,
                this::checkBasketStateAndRedeem,
                dsidBlacklistRepository,
                spendTransactionData -> transactionRepository.addSpendData(new BasketSpendTransactionData(basket.getBasketId(), promotion, requestedTokenUpdate, spendTransactionData))
        );
    }

    IStoreBasketRedeemedHandler.BasketRedeemState checkBasketStateAndRedeem(UUID basketId, BigInteger promotionId, byte[] hash) {
        BasketEntity basketEntity = basketRepository.findById(basketId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find basket with id %s", basketId)));
        var redeemedHashOptional = basketEntity.getRedeemHashForPromotionId(promotionId);
        if (redeemedHashOptional.isEmpty()) {
            basketEntity.setRedeemHashForPromotionId(promotionId, hash);
            basketRepository.save(basketEntity);
            return IStoreBasketRedeemedHandler.BasketRedeemState.BASKET_NOT_REDEEMED;
        }

        if (Arrays.equals(redeemedHashOptional.get(), hash)) {
            return IStoreBasketRedeemedHandler.BasketRedeemState.BASKED_REDEEMED_RETRY;
        } else {
            return IStoreBasketRedeemedHandler.BasketRedeemState.BASKET_REDEEMED_ABORT;
        }
    }

    public String[] getPromotions() {
        return promotionRepository.getPromotions().stream().map(RepresentableRepresentation::new).map(jsonConverter::serialize).toArray(String[]::new);
    }

    public void addPromotions(List<String> serializedPromotions) {
        for (String serializedPromotion : serializedPromotions) {
            RepresentableRepresentation representableRepresentation = (RepresentableRepresentation) jsonConverter.deserialize(serializedPromotion);
            Promotion promotion = (Promotion) representableRepresentation.recreateRepresentable();
            promotionRepository.addPromotion(promotion);
        }
    }

    public void deleteAllPromotions() {
        promotionRepository.deleteAllPromotions();
    }
}
