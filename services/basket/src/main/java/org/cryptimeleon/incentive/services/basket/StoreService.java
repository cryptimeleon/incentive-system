package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.client.dto.store.BulkRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.EarnRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.SpendRequestStoreDto;
import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.model.EarnStoreRequest;
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.SpendCouponRequest;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.ContextManager;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.incentive.services.basket.repository.DsidBlacklistRepository;
import org.cryptimeleon.incentive.services.basket.repository.PromotionRepository;
import org.cryptimeleon.incentive.services.basket.repository.TransactionRepository;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;
import org.cryptimeleon.incentive.services.basket.storage.BasketRepository;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
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

    @Autowired
    private StoreService(CryptoRepository cryptoRepository,
                         PromotionRepository promotionRepository,
                         BasketRepository basketRepository,
                         DsidBlacklistRepository dsidBlacklistRepository,
                         TransactionRepository transactionRepository) {
        this.cryptoRepository = cryptoRepository;
        this.promotionRepository = promotionRepository;
        this.basketRepository = basketRepository;
        this.dsidBlacklistRepository = dsidBlacklistRepository;
        this.transactionRepository = transactionRepository;
    }

    public String registerUserAndReturnSerializedRegistrationCoupon(String serializedUserPublicKey, String userInfo) {
        UserPublicKey userPublicKey = new UserPublicKey(jsonConverter.deserialize(serializedUserPublicKey), cryptoRepository.getPublicParameters());
        RegistrationCoupon registrationCoupon = cryptoRepository.getIncentiveSystem().signRegistrationCoupon(cryptoRepository.getStoreKeyPair(), userPublicKey, userInfo);
        return jsonConverter.serialize(registrationCoupon.getRepresentation());
    }

    public void bulk(BulkRequestStoreDto bulkRequestStoreDto) {
        // Verification
        UUID basketId = bulkRequestStoreDto.getBasketId();
        BasketEntity basketEntity = basketRepository.findById(basketId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find basket with id %s", basketId)));

        // Lock basket
        basketEntity.setLocked(true);
        basketRepository.save(basketEntity);

        // Redeemed handling in individual requests

        // Process all earn and spend requests and store results(TODO in parallel?, check if duplicate promotionIds?)
        Basket basket = promotionBasketFromBasketEntity(basketEntity);
        bulkRequestStoreDto.getEarnRequestStoreDtoList().forEach(earnRequestStoreDto ->
            earn(earnRequestStoreDto, basket)
        );
        bulkRequestStoreDto.getSpendRequestStoreDtoList().forEach(spendRequestStoreDto->
                spend(spendRequestStoreDto, basket)
        );
    }

    public void bulkResponses(UUID basketId) {
        // Verification
        BasketEntity basketEntity = basketRepository.findById(basketId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find basket with id %s", basketId)));

        if (basketEntity.isPaid()) {
            throw new StoreException("This basket is not paid!");
        }

        // Return results

    }
    public void earn(EarnRequestStoreDto earnRequestStoreDto, Basket basket) {
        var promotionId = earnRequestStoreDto.getPromotionId();
        EarnStoreRequest earnStoreRequest = new EarnStoreRequest(jsonConverter.deserialize(earnRequestStoreDto.getSerializedRequest()));
        Promotion promotion = promotionRepository.getPromotion(promotionId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find promotion with id %s", promotionId)));

        Vector<BigInteger> deltaK = promotion.computeEarningsForBasket(basket);

        var signedEarnCoupon = cryptoRepository.getIncentiveSystem().signEarnCoupon(
                cryptoRepository.getStoreKeyPair(),
                deltaK,
                earnStoreRequest,
                basket.getBasketId(),
                promotionId,
                this::checkBasketStateAndRedeem
        );
        var result = jsonConverter.serialize(signedEarnCoupon.getRepresentation());
        // TODO store this
    }

    public void spend(SpendRequestStoreDto spendRequestStoreDto, Basket basket) {
        var serializedSpendStoreRequest =  spendRequestStoreDto.getSerializedRequest();
        var promotionId = spendRequestStoreDto.getPromotionId();
        UUID tokenUpdateId = spendRequestStoreDto.getTokenUpdateId();
        String serializedZkpTokenUpdateMetadata = spendRequestStoreDto.getSerializedTokenUpdateMetadata();


        Promotion promotion = promotionRepository.getPromotion(promotionId)
                .orElseThrow(() -> new StoreException(String.format("Cannot find promotion with id %s", promotionId)));
        ZkpTokenUpdateMetadata zkpTokenUpdateMetadata = (ZkpTokenUpdateMetadata) ((RepresentableRepresentation) jsonConverter.deserialize(serializedZkpTokenUpdateMetadata)).recreateRepresentable();

        ZkpTokenUpdate requestedTokenUpdate =promotion.getZkpTokenUpdates().stream().filter(zkpTokenUpdate -> zkpTokenUpdate.getTokenUpdateId().equals(tokenUpdateId)).findAny()
                .orElseThrow(() -> new StoreException(String.format("Cannot find token update with id %s in promotion with id %s", tokenUpdateId, promotion)));

        // Include value of basket into spend, i.e. users need to spend fewer points from the token if their basket is worth some
        Vector<BigInteger> basketValueForUpdate = promotion.computeEarningsForBasket(basket);
        SpendDeductTree relationTree = requestedTokenUpdate.generateRelationTree(basketValueForUpdate, zkpTokenUpdateMetadata);
        UniqueByteRepresentable context = ContextManager.computeContext(tokenUpdateId, zkpTokenUpdateMetadata);

        SpendCouponRequest spendCouponRequest = new SpendCouponRequest(jsonConverter.deserialize(serializedSpendStoreRequest),
                cryptoRepository.getPublicParameters(),
                basket.getBasketId(),
                promotion.getPromotionParameters(),
                cryptoRepository.getProviderPublicKey(),
                relationTree,
                context);

        var result = cryptoRepository.getIncentiveSystem().signSpendCoupon(
                cryptoRepository.getStoreKeyPair(),
                cryptoRepository.getProviderPublicKey(),
                basket.getBasketId(),
                promotion.getPromotionParameters(),
                spendCouponRequest,
                relationTree,
                context,
                this::checkBasketStateAndRedeem,
                dsidBlacklistRepository,
                transactionRepository
        );
        // TODO store this
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
