package org.cryptimeleon.incentive.services.promotion.repository;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.dto.BasketDto;
import org.cryptimeleon.incentive.client.dto.BasketItemDto;
import org.cryptimeleon.incentive.client.dto.PostRedeemBasketDto;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repository that is a wrapper around the basket client.
 * Used for communication with the basket service to verify basket of earn request.
 */
@Repository
public class BasketRepository {
    @Value("${basket-service.redeem-secret}")
    private String redeemSecret;
    private BasketClient basketClient;

    @Autowired
    public BasketRepository(BasketClient basketClient) {
        this.basketClient = basketClient;
    }

    public BasketDto getBasketDto(UUID basketId) {
        return basketClient.getBasket(basketId)
                .block(Duration.ofSeconds(1));
    }

    public Basket getBasket(UUID basketId) {
        BasketDto basketDto = basketClient.getBasket(basketId).block(Duration.ofSeconds(1));
        BasketItemDto[] items = basketClient.getItems().block(Duration.ofSeconds(1));

        assert items != null;
        assert basketDto != null;

        return new Basket(
                basketId,
                basketDto.getItems().entrySet().stream()
                        .map(stringIntegerEntry -> {
                            var basketItem = Arrays.stream(items).filter(item -> item.getId().equals(stringIntegerEntry.getKey())).findAny().get();
                            return new BasketItem(UUID.fromString(basketItem.getId()), basketItem.getTitle(), basketItem.getPrice(), stringIntegerEntry.getValue());
                        })
                        .collect(Collectors.toList())
        );
    }


    public void redeem(UUID basketId, String redeemRequestText, long value) {
        var redeemRequest = new PostRedeemBasketDto(basketId, redeemRequestText, value);
        basketClient.redeemBasket(redeemRequest, redeemSecret)
                .block(Duration.ofSeconds(1));
    }
}
