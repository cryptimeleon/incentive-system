package org.cryptimeleon.incentivesystem.services.credit.model.interfaces;

import org.cryptimeleon.incentivesystem.client.dto.BasketDto;
import org.cryptimeleon.incentivesystem.services.credit.IncentiveException;

import java.util.UUID;

public interface BasketServerClientInterface {
    BasketDto getBasket(UUID basketId) throws IncentiveException;

    void redeem(UUID basketId, String redeemRequest, long value) throws IncentiveException;
}
