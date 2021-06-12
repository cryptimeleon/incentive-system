package org.cryptimeleon.incentivesystem.client.integrationtest;

import org.cryptimeleon.incentivesystem.client.BasketserverClient;
import org.cryptimeleon.incentivesystem.client.dto.BasketDto;

import java.util.UUID;


public class TestHelper {
    static BasketDto createBasketWithItems(String basketserverUrl) {
        var basketServerClient = new BasketserverClient(basketserverUrl);

        UUID basketId = basketServerClient.createBasket().block();
        var items = basketServerClient.getItems().block();
        var firstTestItem = items[0];
        var secondTestItem = items[1];

        basketServerClient.putItemToBasket(basketId, firstTestItem.getId(), 3).block();
        basketServerClient.putItemToBasket(basketId, secondTestItem.getId(), 1).block();

        return basketServerClient.getBasket(basketId).block();
    }
}
