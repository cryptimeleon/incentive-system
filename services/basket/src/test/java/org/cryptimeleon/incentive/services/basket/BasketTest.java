package org.cryptimeleon.incentive.services.basket;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.services.basket.model.BasketItem;
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

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasketTest {

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
    void queryBasketQueryParamTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        queryBasketUrlParam(webTestClient, basketId, HttpStatus.OK);
    }

    @Test
    void queryBasketHeaderParamTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        queryBasket(webTestClient, basketId, HttpStatus.OK);
    }

    @Test
    void createBasketTest(@Autowired WebTestClient webClient) {
        log.info("Creating new basket");
        var createResponse = createBasket(webClient);
        log.info("Create response: " + createResponse);
        UUID basketId = createResponse.getResponseBody();
        log.info("Querying basket");
        var basketResponse = queryBasket(webClient, basketId);

        var basket = basketResponse.getResponseBody();
        log.info("Basket response: " + basket);

        assert basket != null;
        assertThat(basket.getBasketItems()).isEmpty();
        assertThat(basket.isPaid()).isFalse();
        assertThat(basket.isRedeemed()).isFalse();
        assertThat(basket.getBasketID()).isEqualByComparingTo(basketId);
    }

    @Test
    void deleteBasketTest(@Autowired WebTestClient webClient) {
        UUID basketId = createBasket(webClient).getResponseBody();

        deleteBasket(webClient, basketId);

        queryBasket(webClient, basketId, HttpStatus.NOT_FOUND);
    }

    @Test
    void basketAddNegativeItemCountTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        putItem(webTestClient, basketId, firstTestItem.getId(), -2, HttpStatus.UNPROCESSABLE_ENTITY);
        putItem(webTestClient, basketId, firstTestItem.getId(), 0, HttpStatus.UNPROCESSABLE_ENTITY);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assert basket != null;
        assertThat(basket.getBasketItems()).isEmpty();
    }

    @Test
    void basketAddInvalidItemsTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        putItem(webTestClient, basketId, "1234123412", 2, HttpStatus.NOT_FOUND);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assert basket != null;
        assertThat(basket.getBasketItems()).isEmpty();
    }


    @Test
    void basketAddItemsTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assert basket != null;
        assertThat(basket.getBasketItems())
                .contains(new BasketItem(firstTestItem, 5))
                .contains(new BasketItem(secondTestItem, 1));
    }

    @Test
    void basketOverwriteItemsTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.OK);
        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK); // Test updating works as expected

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assert basket != null;
        assertThat(basket.getBasketItems())
                .contains(new BasketItem(firstTestItem, 3));
    }

    @Test
    void basketDeleteItemsTest(@Autowired WebTestClient webTestClient) {
        UUID basketId = createBasket(webTestClient).getResponseBody();

        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.OK);

        deleteBasketItem(webTestClient, basketId, firstTestItem.getId(), HttpStatus.OK);
        var basket = queryBasket(webTestClient, basketId).getResponseBody();

        assert basket != null;
        assertThat(basket.getBasketItems()).isEmpty();
    }
}
