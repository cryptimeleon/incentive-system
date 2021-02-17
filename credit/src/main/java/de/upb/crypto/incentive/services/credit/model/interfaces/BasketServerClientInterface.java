package de.upb.crypto.incentive.services.credit.model.interfaces;

import de.upb.crypto.incentive.client.dto.BasketDto;
import de.upb.crypto.incentive.services.credit.IncentiveException;

import java.util.UUID;

public interface BasketServerClientInterface {
    BasketDto getBasket(UUID basketId) throws IncentiveException;

    void redeem(UUID basketId, String redeemRequest, long value) throws IncentiveException;
}
