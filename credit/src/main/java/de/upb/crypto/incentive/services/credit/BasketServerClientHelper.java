package de.upb.crypto.incentive.services.credit;

import de.upb.crypto.incentive.client.BasketserverClient;
import de.upb.crypto.incentive.client.dto.BasketDto;
import de.upb.crypto.incentive.client.dto.PostRedeemBasketDto;
import de.upb.crypto.incentive.services.credit.model.interfaces.BasketServerClientInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/*
 * Mock for use until basket server is merged into develop
 */
public class BasketServerClientHelper implements BasketServerClientInterface {

    Logger logger = LoggerFactory.getLogger(BasketServerClientHelper.class);

    private String basketServerUrl;
    private String redeemSecret;
    private BasketserverClient basketServerClient;

    public BasketServerClientHelper(String basketServerUrl, String redeemSecret) {
        this.redeemSecret = redeemSecret;
        this.basketServerUrl = basketServerUrl;
        this.basketServerClient = new BasketserverClient(basketServerUrl);
    }

    @Override
    public BasketDto getBasket(UUID basketId) {
        return basketServerClient.getBasket(basketId)
                .block();
    }

    @Override
    public void redeem(UUID basketId, String redeemRequestText, long value) {
        var redeemRequest = new PostRedeemBasketDto(basketId, redeemRequestText, value);
        basketServerClient.redeemBasket(redeemRequest, redeemSecret)
                .block();
    }
}
