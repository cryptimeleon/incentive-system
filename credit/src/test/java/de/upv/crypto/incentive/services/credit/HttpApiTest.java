package de.upv.crypto.incentive.services.credit;

import de.upb.crypto.incentive.protocoldefinition.creditearn.CreditResponse;
import de.upb.crypto.incentive.protocoldefinition.creditearn.EarnRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpApiTest {

  private java.net.URI buildRequestUri(UriBuilder uriBuilder, EarnRequest earnRequest) {
    return uriBuilder
        .path("/credit")
        .queryParam("id", earnRequest.getId())
        .queryParam("serializedEarnRequest", earnRequest.getSerializedEarnRequest())
        .queryParam("earnAmount", earnRequest.getEarnAmount())
        .queryParam("basketId", earnRequest.getBasketId())
        .build();
  }

  @Test
  void validRequestTest(@Autowired WebTestClient webClient) {
    UUID id = UUID.randomUUID();
    UUID basketId = UUID.randomUUID();
    webClient
        .get()
        .uri(
            uriBuilder ->
                buildRequestUri(uriBuilder, new EarnRequest(id, "Some request", 5, basketId)))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(CreditResponse.class)
        .isEqualTo(new CreditResponse(id, "Test credit response"));
  }

  @Test
  void invalidRequestTest(@Autowired WebTestClient webClient) {
    UUID id = UUID.randomUUID();
    UUID basketId = UUID.randomUUID();
    webClient
        .get()
        .uri(
            uriBuilder ->
                buildRequestUri(uriBuilder, new EarnRequest(id, "Some request", -5, basketId)))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.FORBIDDEN);
  }
}
