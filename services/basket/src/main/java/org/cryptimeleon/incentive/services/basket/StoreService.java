package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.model.EarnStoreRequest;
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.incentive.services.basket.repository.PromotionRepository;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;
import org.cryptimeleon.incentive.services.basket.storage.BasketRepository;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final CryptoRepository cryptoRepository;
    private final PromotionRepository promotionRepository;
    private final JSONConverter jsonConverter = new JSONConverter();
    private final BasketRepository basketRepository;

    @Autowired
    private StoreService(CryptoRepository cryptoRepository, PromotionRepository promotionRepository,
                         BasketRepository basketRepository) {
        this.cryptoRepository = cryptoRepository;
        this.promotionRepository = promotionRepository;
        this.basketRepository = basketRepository;
    }

    public String registerUserAndReturnSerializedRegistrationCoupon(String serializedUserPublicKey, String userInfo) {
        UserPublicKey userPublicKey = new UserPublicKey(jsonConverter.deserialize(serializedUserPublicKey), cryptoRepository.getPublicParameters());
        RegistrationCoupon registrationCoupon = cryptoRepository.getIncentiveSystem().signRegistrationCoupon(cryptoRepository.getStoreKeyPair(), userPublicKey, userInfo);
        return jsonConverter.serialize(registrationCoupon.getRepresentation());
    }

    public String earn(String serializedEarnStoreRequest) {
        EarnStoreRequest earnStoreRequest = new EarnStoreRequest(jsonConverter.deserialize(serializedEarnStoreRequest));
        Promotion promotion = promotionRepository.getPromotion(earnStoreRequest.getPromotionId())
                .orElseThrow(() -> new StoreException(String.format("Cannot find promotion with id %s", earnStoreRequest.getPromotionId())));
        BasketEntity basketEntity = basketRepository.findById(earnStoreRequest.getBasketId())
                .orElseThrow(() -> new StoreException(String.format("Cannot find basket with id %s", earnStoreRequest.getBasketId())));
        Basket basket = promotionBasketFromBasketEntity(basketEntity);


        Vector<BigInteger> deltaK = promotion.computeEarningsForBasket(basket);

        var signedEarnCoupon = cryptoRepository.getIncentiveSystem().signEarnCoupon(
                cryptoRepository.getStoreKeyPair(),
                deltaK,
                earnStoreRequest,
                // TODO implement this properly
                (basketId, promotionId, hash) -> IStoreBasketRedeemedHandler.BasketRedeemState.BASKET_NOT_REDEEMED
        );

        return jsonConverter.serialize(signedEarnCoupon.getRepresentation());
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
