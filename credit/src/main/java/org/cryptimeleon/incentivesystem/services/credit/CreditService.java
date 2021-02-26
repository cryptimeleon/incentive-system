package org.cryptimeleon.incentivesystem.services.credit;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.cryptimeleon.incentivesystem.services.credit.interfaces.CreditInterface;
import org.cryptimeleon.incentivesystem.services.credit.model.CreditResponse;
import org.cryptimeleon.incentivesystem.services.credit.model.EarnRequest;
import org.cryptimeleon.incentivesystem.services.credit.model.interfaces.BasketServerClientInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CreditService {
    Logger logger = LoggerFactory.getLogger(CreditService.class);

    @NonNull
    private CreditInterface cryptoCreditHandler;
    @NonNull
    private BasketServerClientInterface basketServerClient;

    public CreditResponse handleEarnRequest(EarnRequest request) throws IncentiveException {
        // verify earnAmount
        var requestId = request.getId();
        var basketId = request.getBasketId();
        var earnRequest = request.getSerializedEarnRequest();

        logger.info("EarnRequest:" + earnRequest);
        // Validations
        var basket = basketServerClient.getBasket(basketId);
        logger.info("Queried basket:" + basket);
        if (!basket.isPaid()) {
            throw new IncentiveException("Basket not paid");
        }
        if (basket.isRedeemed() && !basket.getRedeemRequest().equals(earnRequest)) {
            throw new IncentiveException("Basket was redeemed with another request!");
        }

        if (!basket.isRedeemed()) {
            basketServerClient.redeem(basketId, earnRequest, basket.getValue());
            // TODO think about when to redeem
            // Maybe add some kind of lock mechanism that only sends the basket to redeemed after the response was generated
        }

        var creditResponse = cryptoCreditHandler.computeSerializedResponse(earnRequest, basket.getValue());

        return new CreditResponse(requestId, creditResponse);
    }
}
