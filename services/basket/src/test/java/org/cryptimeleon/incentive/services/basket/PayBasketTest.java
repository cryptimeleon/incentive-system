package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.services.basket.model.Item;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.services.basket.ClientHelper.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PayBasketTest {
    @Value("${basket-service.provider-secret}")
    private String providerSecret;

    @Value("${basket-service.pay-secret}")
    private String paymentSecret;

    private final Item firstTestItem = new Item("23578", "First test item", 235);
    private final Item secondTestItem = new Item("1234554", "Second test item", 123);

    @BeforeAll
    void addTestItems(@Autowired WebTestClient webTestClient) {
        ClientHelper.newItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);
        ClientHelper.newItem(webTestClient, secondTestItem, providerSecret, HttpStatus.OK);
    }

    @Test
    public void payBasketWithItemsTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();
        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);

        payBasket(webTestClient, basketId, paymentSecret, HttpStatus.OK);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assert basket != null;
        assertThat(basket.isPaid()).isTrue();
    }

    @Test
    public void payBasketAuthorizationTest(@Autowired WebTestClient webTestClient) {
        payBasket(webTestClient, UUID.randomUUID(), "", HttpStatus.UNAUTHORIZED);
        payBasket(webTestClient, UUID.randomUUID(), paymentSecret + "x", HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void payNotExistentBasketTest(@Autowired WebTestClient webTestClient) {
        var wrongBasketId = UUID.randomUUID();

        payBasket(webTestClient, wrongBasketId, paymentSecret, HttpStatus.NOT_FOUND);
    }

    @Test
    public void payEmptyBasketTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        payBasket(webTestClient, basketId, paymentSecret, HttpStatus.BAD_REQUEST);
    }


    @Test
    public void paidBasketImmutableTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();
        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);
        payBasket(webTestClient, basketId, paymentSecret, HttpStatus.OK);

        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.BAD_REQUEST);
        deleteBasketItem(webTestClient, basketId, secondTestItem.getId(), HttpStatus.BAD_REQUEST);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assert basket != null;
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .containsEntry(secondTestItem.getId(), 1);
    }
}
