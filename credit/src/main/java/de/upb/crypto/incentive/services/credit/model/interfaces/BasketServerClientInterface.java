package de.upb.crypto.incentive.services.credit.model.interfaces;

import de.upb.crypto.incentive.services.credit.model.Basket;

import java.util.UUID;

public interface BasketServerClientInterface {
    Basket getBasket(UUID basketId);

    void redeem(UUID basketId, String redeemRequest, long value);
}
