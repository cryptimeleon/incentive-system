package de.upb.crypto.incentive.services.credit.mock;

import de.upb.crypto.incentive.services.credit.model.interfaces.BasketServerClientInterface;
import de.upb.crypto.incentive.services.credit.model.Basket;

import java.util.Collections;
import java.util.UUID;

/*
 * Mock for use until basket server is merged into develop
 */
public class BasketServerClientMock implements BasketServerClientInterface {

    @Override
    public Basket getBasket(UUID basketId) {
        var basket = new Basket();
        var items = Collections.singletonMap(UUID.randomUUID(), 42);
        basket.setBasketID(basketId);
        basket.setPaid(true);
        basket.setItems(items);
        basket.setRedeemed(false);
        return basket;
    }

    @Override
    public void redeem(UUID basketId, String redeemRequest, long value) {
        return;
    }
}
