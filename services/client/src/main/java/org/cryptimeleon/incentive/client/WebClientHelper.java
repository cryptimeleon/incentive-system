package org.cryptimeleon.incentive.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientHelper {
    /**
     * Simple helper that returns a WebClient for a given url.
     * Throws IncentiveClientException if an HTTP error occurs.
     *
     * @param url target url of the webclient
     * @return webclient object
     */
    static WebClient buildWebClient(String url) {
        return WebClient
                .builder()
                .baseUrl(url)
                .filter(ExchangeFilterFunctions.statusError(HttpStatus::isError, clientResponse -> new IncentiveClientException()))
                .build();
    }
}
