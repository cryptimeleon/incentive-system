package org.cryptimeleon.incentive.services.store;

import org.cryptimeleon.incentive.services.store.api.BasketItem;
import org.cryptimeleon.incentive.services.store.api.Item;
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
import static org.cryptimeleon.incentive.services.store.ClientHelper.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PayBasketTest {
    private final Item firstTestItem = new Item("23578", "First test item", 235);
    private final Item secondTestItem = new Item("1234554", "Second test item", 123);
    @Value("${basket-service.provider-secret}")
    private String providerSecret;

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

        payBasket(webTestClient, basketId, HttpStatus.OK);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assert basket != null;
        assertThat(basket.isPaid()).isTrue();
    }

    @Test
    public void payNotExistentBasketTest(@Autowired WebTestClient webTestClient) {
        var wrongBasketId = UUID.randomUUID();

        payBasket(webTestClient, wrongBasketId, HttpStatus.NOT_FOUND);
    }

    @Test
    public void payEmptyBasketTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        payBasket(webTestClient, basketId, HttpStatus.BAD_REQUEST);
    }


    @Test
    public void paidBasketImmutableTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();
        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);
        payBasket(webTestClient, basketId, HttpStatus.OK);

        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.BAD_REQUEST);
        deleteBasketItem(webTestClient, basketId, secondTestItem.getId(), HttpStatus.BAD_REQUEST);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assert basket != null;
        assertThat(basket.getBasketItems())
                .contains(new BasketItem(firstTestItem, 3))
                .contains(new BasketItem(secondTestItem, 1));
    }
}