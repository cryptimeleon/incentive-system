package org.cryptimeleon.incentive.services.credit.interfaces;

import org.cryptimeleon.incentive.client.dto.BasketDto;
import org.cryptimeleon.incentive.services.credit.IncentiveException;

import java.util.UUID;

public interface BasketClientInterface {
    BasketDto getBasket(UUID basketId) throws IncentiveException;

    void redeem(UUID basketId, String redeemRequest, long value) throws IncentiveException;
}
