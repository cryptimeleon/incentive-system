package de.upb.crypto.incentive.services.credit;

import de.upb.crypto.incentive.basketserver.BasketController;
import de.upb.crypto.incentive.basketserver.client.BasketServerClient;
import de.upb.crypto.incentive.basketserver.model.Basket;
import de.upb.crypto.incentive.basketserver.model.requests.RedeemBasketRequest;
import de.upb.crypto.incentive.services.credit.model.interfaces.BasketServerClientInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/*
 * Mock for use until basket server is merged into develop
 */
public class BasketServerClientHelper implements BasketServerClientInterface {

    Logger logger = LoggerFactory.getLogger(BasketController.class);

    @Value("${basketserver.url}")
    private String basketServerUrl;
    private BasketServerClient basketServerClient;

    private WebClient basketWebClient;

    @Value("${basketserver.redeem-secret}")
    private String redeemSecret;

    /*
     * Convert 4xx and 5xx into BasketServerException
     */
    public ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError() || clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            logger.info("Basket server " + clientResponse.statusCode() + "exception: " + errorBody);
                            return Mono.error(new BasketServerException(errorBody, clientResponse.statusCode()));
                        });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }

    public BasketServerClientHelper() {
        this.basketWebClient = WebClient.builder()
                .baseUrl(basketServerUrl)
                .filter(errorHandlingFilter())
                .build();
        this.basketServerClient = new BasketServerClient(basketWebClient, "", redeemSecret);
    }

    @Override
    public Basket getBasket(UUID basketId) {
        return basketServerClient.getBasket(basketId)
                .block();
    }

    @Override
    public void redeem(UUID basketId, String redeemRequestText, long value) {
        var redeemRequest = new RedeemBasketRequest(basketId, redeemRequestText, value);
        basketServerClient.redeem(redeemRequest)
                .block();
    }
}
