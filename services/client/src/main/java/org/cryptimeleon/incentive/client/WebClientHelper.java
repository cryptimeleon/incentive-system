package org.cryptimeleon.incentive.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientHelper {
    static WebClient buildWebClient(String url) {
        return WebClient
                .builder()
                .baseUrl(url)
                .filter(ExchangeFilterFunctions.statusError(HttpStatus::isError, clientResponse -> new IncentiveClientException()))
                .build();
    }
}
