package org.cryptimeleon.incentive.services.credit.mock;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.cryptimeleon.incentive.client.dto.BasketDto;
import org.cryptimeleon.incentive.services.credit.interfaces.BasketClientInterface;

import java.util.ArrayList;
import java.util.UUID;

@NoArgsConstructor
@Data
public class TestBasketClientMock implements BasketClientInterface {

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
