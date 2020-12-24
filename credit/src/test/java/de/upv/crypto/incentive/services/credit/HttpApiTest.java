package de.upv.crypto.incentive.services.credit;

import de.upb.crypto.incentive.protocols.credit.CreditRequest;
import de.upb.crypto.incentive.protocols.credit.CreditResponse;
import de.upb.crypto.incentive.protocols.model.Token;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpApiTest {

    private java.net.URI buildRequestUri(UriBuilder uriBuilder, CreditRequest creditRequest) {
        return uriBuilder
                .path("/credit")
                .queryParam("id", creditRequest.getId())
                .queryParam("token.value", creditRequest.getToken().getValue())
                .queryParam("increase", creditRequest.getIncrease())
                .queryParam("basketId", creditRequest.getBasketId())
                .build();
    }

    @Test
    void validRequestTest(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri(uriBuilder -> buildRequestUri(uriBuilder, new CreditRequest(42, new Token(17), 5, 99)))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreditResponse.class)
                .isEqualTo(new CreditResponse(42, new Token(17 + 5)));
    }

    @Test
    void invalidRequestTest(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri(uriBuilder -> buildRequestUri(uriBuilder, new CreditRequest(42, new Token(17), -3, 99)))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
