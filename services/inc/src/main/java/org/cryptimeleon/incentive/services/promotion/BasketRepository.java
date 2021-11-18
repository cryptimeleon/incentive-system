package org.cryptimeleon.incentive.services.promotion;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.dto.BasketDto;
import org.cryptimeleon.incentive.client.dto.PostRedeemBasketDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

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

    public BasketDto getBasket(UUID basketId) {
        return basketClient.getBasket(basketId)
                .block(Duration.ofSeconds(1));
    }

    public void redeem(UUID basketId, String redeemRequestText, long value) {
        var redeemRequest = new PostRedeemBasketDto(basketId, redeemRequestText, value);
        basketClient.redeemBasket(redeemRequest, redeemSecret)
                .block(Duration.ofSeconds(1));
    }
}
