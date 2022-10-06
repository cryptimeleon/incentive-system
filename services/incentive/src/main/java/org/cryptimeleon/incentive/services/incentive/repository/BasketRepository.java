package org.cryptimeleon.incentive.services.incentive.repository;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.dto.BasketDto;
import org.cryptimeleon.incentive.client.dto.BasketItemDto;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Repository that is a wrapper around the basket client.
 * Used for communication with the basket service to verify basket of earn request.
 */
@Slf4j
@Repository
public class BasketRepository {
    @Value("${basket-service.redeem-secret}")
    private String redeemSecret;

    private final BasketClient basketClient;

    @Autowired
    public BasketRepository(BasketClient basketClient) {
        this.basketClient = basketClient;
    }

    @PostConstruct
    public void validateValue() {
        if (redeemSecret.equals("")) {
            throw new IllegalArgumentException("Redeem secret is not set!");
        }
        log.info("redeem secret: {}", redeemSecret);
    }

    public BasketDto getBasketDto(UUID basketId) {
        return basketClient.getBasket(basketId)
                .block(Duration.ofSeconds(1));
    }

    /**
     * Returns true if and only if the basket with the passed basket ID is paid.
     */
    public boolean isBasketPaid(UUID basketId) {
        return Objects.requireNonNull(basketClient.getBasket(basketId).block(Duration.ofSeconds(1))).isPaid();
    }

    /**
     * Returns the basket with the basket with the passed ID if such a basket is saved on the basket server.
     * Wrapper around the basket client.
     */
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
                            return new BasketItem(basketItem.getId(), basketItem.getTitle(), basketItem.getPrice(), stringIntegerEntry.getValue());
                        })
                        .collect(Collectors.toList())
        );
    }

    /**
     * Locks the basket with the passed basket ID, meaning that no more items can be added to it.
     * This is needed since only locked baskets can be used to earn points and claim rewards.
     */
    public void lockBasket(UUID basketId) {
        basketClient.lockBasket(basketId, redeemSecret).block();
    }

    /**
     * Sets the list of rewards (identified by IDs) that the user could claim for the basket with the passed ID.
     * The uses who owns the basket can claim these rewards as soon as she has paid the basket.
     */
    public void setRewardsOfBasket(UUID basketId, ArrayList<String> rewardIds) {
        basketClient.setRewardsForBasket(basketId, rewardIds, redeemSecret).block();
    }
}
