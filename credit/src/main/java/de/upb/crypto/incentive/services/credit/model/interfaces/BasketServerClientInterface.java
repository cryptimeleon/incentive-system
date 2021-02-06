package de.upb.crypto.incentive.services.credit.model.interfaces;

import de.upb.crypto.incentive.basketserver.model.Basket;
import de.upb.crypto.incentive.services.credit.IncentiveException;

import java.util.UUID;

public interface BasketServerClientInterface {
    Basket getBasket(UUID basketId) throws IncentiveException;

    void redeem(UUID basketId, String redeemRequest, long value) throws IncentiveException;
}
