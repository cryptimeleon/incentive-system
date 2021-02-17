package de.upb.crypto.incentive.services.credit.mock;

import de.upb.crypto.incentive.client.dto.BasketDto;
import de.upb.crypto.incentive.services.credit.model.interfaces.BasketServerClientInterface;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.UUID;

@NoArgsConstructor
@Data
public class TestBasketServerClientMock implements BasketServerClientInterface {

    private ArrayList<BasketDto> baskets;

    @Override
    public BasketDto getBasket(UUID basketId) {
        return baskets.stream().filter(b -> b.getBasketID().equals(basketId)).findFirst().get();
    }

    @Override
    public void redeem(UUID basketId, String redeemRequest, long value) {
        var basket = baskets.stream().filter(b -> b.getBasketID().equals(basketId)).findFirst().get();
        if (basket.isRedeemed() && !basket.getRedeemRequest().equals(redeemRequest)) {
            throw new RuntimeException();  // TODO replace by the exceptions thrown by the actual client
        }
        if (basket.getValue() != value) {
            throw new RuntimeException();
        }
        basket.setRedeemed(true);
        basket.setRedeemRequest(redeemRequest);
    }
}
