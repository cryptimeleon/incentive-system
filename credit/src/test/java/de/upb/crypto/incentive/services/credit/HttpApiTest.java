package de.upb.crypto.incentive.services.credit;

import de.upb.crypto.incentive.client.dto.BasketDto;
import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.CreditInterface;
import de.upb.crypto.incentive.services.credit.mock.TestBasketServerClientMock;
import de.upb.crypto.incentive.services.credit.mock.TestCryptoCreditMock;
import de.upb.crypto.incentive.services.credit.model.CreditResponse;
import de.upb.crypto.incentive.services.credit.model.EarnRequest;
import de.upb.crypto.incentive.services.credit.model.interfaces.BasketServerClientInterface;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Data
@Import(TestConfiguration.class)
// Use test configuration that injects TestBasketServerClient and TestCryptoCredit for unit tests
public class HttpApiTest {

    final String validRequest = "validRequest";
    final String validResponse = "validResponse";
    final String invalidResponse = "invalidResponse";

    @Autowired
    private CreditInterface creditInterfaceBean;
    @Autowired
    private BasketServerClientInterface basketServerClientInterfaceBean;

    private TestCryptoCreditMock testCryptoCreditMock = null;
    private TestBasketServerClientMock testBasketServerClientMock = null;

    private java.net.URI buildRequestUri(UriBuilder uriBuilder, EarnRequest earnRequest) {
        return uriBuilder
                .path("/credit")
                .queryParam("id", earnRequest.getId())
                .queryParam("serializedEarnRequest", earnRequest.getSerializedEarnRequest())
                .queryParam("basketId", earnRequest.getBasketId())
                .build();
    }

    private void prepareTests() {
        testBasketServerClientMock = (TestBasketServerClientMock) basketServerClientInterfaceBean;
        testCryptoCreditMock = (TestCryptoCreditMock) creditInterfaceBean;

        testCryptoCreditMock.setValidRequest(validRequest);
        testCryptoCreditMock.setValidResponse(validResponse);
        testCryptoCreditMock.setInvalidResponse(invalidResponse);
        testBasketServerClientMock.setBaskets(new ArrayList<>());
    }

    @Test
    void validRequestTest(@Autowired WebTestClient webClient) {
        prepareTests();
        var earnAmount = 42;
        var testBasket = new BasketDto();
        var items = Collections.singletonMap(UUID.randomUUID(), earnAmount);
        testBasket.setBasketID(UUID.randomUUID());
        testBasket.setPaid(true);
        testBasket.setItems(items);

        testBasketServerClientMock.getBaskets().add(testBasket);


        UUID id = UUID.randomUUID();
        webClient.get()
                .uri(uriBuilder -> buildRequestUri(uriBuilder, new EarnRequest(id, validRequest, testBasket.getBasketID())))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreditResponse.class)
                .isEqualTo(new CreditResponse(id, validResponse));
    }

    @Test
    void unpaidBasketTest(@Autowired WebTestClient webClient) {
        prepareTests();
        var earnAmount = 42;
        var testBasket = new BasketDto();
        var items = Collections.singletonMap(UUID.randomUUID(), earnAmount);
        testBasket.setBasketID(UUID.randomUUID());
        testBasket.setPaid(false);  // Unpaid baskets cannot be redeemed
        testBasket.setItems(items);
        UUID id = UUID.randomUUID();

        testBasketServerClientMock.getBaskets().add(testBasket);

        webClient.get()
                .uri(uriBuilder -> buildRequestUri(uriBuilder, new EarnRequest(id, validRequest, testBasket.getBasketID())))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
