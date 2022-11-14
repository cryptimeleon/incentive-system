package org.cryptimeleon.incentive.services.basket;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.services.basket.api.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.cryptimeleon.incentive.services.basket.ClientHelper.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedeemTest {

    private final Item firstTestItem = new Item("23578", "First test item", 10);
    private final Item secondTestItem = new Item("1234554", "Second test item", 20);
    @Value("${basket-service.pay-secret}")
    private String paymentSecret;
    @Value("${basket-service.redeem-secret}")
    private String redeemSecret;
    @Value("${basket-service.provider-secret}")
    private String providerSecret;
    private UUID basketId;

    @BeforeAll
    void addTestItems(@Autowired WebTestClient webTestClient) {
        ClientHelper.newItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);
        ClientHelper.newItem(webTestClient, secondTestItem, providerSecret, HttpStatus.OK);
    }

    @BeforeEach
    void prepareReadyToPayBasket(@Autowired WebTestClient webTestClient) {
        basketId = createBasket(webTestClient).getResponseBody();
        ClientHelper.putItem(webTestClient, basketId, firstTestItem.getId(), 2, HttpStatus.OK);
    }

    @Test
    void redeemPaidBasketTest(@Autowired WebTestClient webTestClient) {
        payBasket(webTestClient, basketId, paymentSecret, HttpStatus.OK);
        redeemBasket(webTestClient, basketId, "Some Request", 20, redeemSecret, HttpStatus.OK);
    }

    @Test
    void redeemPaidBasketInvalidRedeemSecretTest(@Autowired WebTestClient webTestClient) {
        payBasket(webTestClient, basketId, paymentSecret, HttpStatus.OK);
        redeemBasket(webTestClient, basketId, "Some Request", 20, "", HttpStatus.UNAUTHORIZED);
        redeemBasket(webTestClient, basketId, "Some Request", 20, redeemSecret + "x", HttpStatus.UNAUTHORIZED);
    }

    @Test
    void redeemNotPaidBasketTest(@Autowired WebTestClient webTestClient) {
        redeemBasket(webTestClient, basketId, "Some Request", 20, redeemSecret, HttpStatus.BAD_REQUEST);
    }

    @Test
    void redeemNotExistingBasketTest(@Autowired WebTestClient webTestClient) {
        redeemBasket(webTestClient, UUID.randomUUID(), "Some Request", 20, redeemSecret, HttpStatus.NOT_FOUND);
    }

    @Test
    void redeemWithWrongValueTest(@Autowired WebTestClient webTestClient) {
        payBasket(webTestClient, basketId, paymentSecret, HttpStatus.OK);
        redeemBasket(webTestClient, basketId, "Some Request", 21, redeemSecret, HttpStatus.BAD_REQUEST);
    }

    @Test
    void reRedeemTest(@Autowired WebTestClient webTestClient) {
        payBasket(webTestClient, basketId, paymentSecret, HttpStatus.OK);
        redeemBasket(webTestClient, basketId, "Some Request", 20, redeemSecret, HttpStatus.OK);

        redeemBasket(webTestClient, basketId, "Some Request", 20, redeemSecret, HttpStatus.OK);
    }

    @Test
    void reRedeemChangedRequestTest(@Autowired WebTestClient webTestClient) {
        payBasket(webTestClient, basketId, paymentSecret, HttpStatus.OK);
        redeemBasket(webTestClient, basketId, "Some Request", 20, redeemSecret, HttpStatus.OK);

        redeemBasket(webTestClient, basketId, "Another Request", 20, redeemSecret, HttpStatus.BAD_REQUEST);
    }
}
