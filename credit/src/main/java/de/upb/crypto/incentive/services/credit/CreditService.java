package de.upb.crypto.incentive.services.credit;

import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.CreditInterface;
import de.upb.crypto.incentive.protocoldefinition.creditearn.CreditResponse;
import de.upb.crypto.incentive.protocoldefinition.creditearn.EarnRequest;
import de.upb.crypto.incentive.services.credit.model.interfaces.BasketServerClientInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CreditService {
    private CreditInterface cryptoCreditHandler;
    private BasketServerClientInterface basketServerClient;

    public CreditResponse handleEarnRequest(EarnRequest request) throws IncentiveException {
        // verify earnAmount
        var requestId = request.getId();
        var earnAmount = request.getEarnAmount();
        var basketId = request.getBasketId();
        var earnRequest = request.getSerializedEarnRequest();

        // Validations
        var basket = basketServerClient.getBasket(basketId);
        if (!basket.isPaid()) {
            throw new IncentiveException("Basket not paid");
        }
        if (basket.isRedeemed() && !basket.getRedeemRequest().equals(earnRequest)) {
            throw new IncentiveException("Basket was redeemed with another request!");
        }
        if (basket.getValue() != earnAmount) {
            throw new IncentiveException("The requested value does not match the basket's value");
        }

        if (!basket.isRedeemed()) {
            basketServerClient.redeem(basketId, earnRequest, earnAmount);
        }

        var creditResponse = cryptoCreditHandler.computeSerializedResponse(earnRequest, earnAmount);

        return new CreditResponse(requestId, creditResponse);
    }
}
