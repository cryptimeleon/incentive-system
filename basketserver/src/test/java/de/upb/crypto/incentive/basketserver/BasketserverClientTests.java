package de.upb.crypto.incentive.basketserver;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static de.upb.crypto.incentive.basketserver.ClientHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BasketserverClientTests {

    Logger logger = LoggerFactory.getLogger(BasketController.class);

    @Test
    void helloWorldTest(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("Hello World");
    }

    @Test
    void createDeleteBasketTest(@Autowired WebTestClient webClient) {
        logger.info("Creating new basket");
        var createResponse = createBasket(webClient);

        logger.info("Create response: " + createResponse);
        UUID basketId = createResponse.getResponseBody();

        logger.info("Querying basket");
        var basketResponse = queryBasket(webClient, basketId);

        var basket = basketResponse.getResponseBody();
        logger.info("Basket response: " + basket);

        assert basket != null;
        assertThat(basket.getItems()).isEmpty();
        assertThat(basket.isPaid()).isFalse();
        assertThat(basket.isRedeemed()).isFalse();
        assertThat(basket.getBasketID()).isEqualByComparingTo(basketId);

        logger.info("Delete Basket");
        deleteBasket(webClient, basketId);

        logger.info("Query deleted basket");
        queryBasket(webClient, basketId, HttpStatus.NOT_FOUND);
    }

    @Test
    void basketItemsTest(@Autowired WebTestClient webTestClient) {
        logger.info("Creating new basket");
        var createResponse = createBasket(webTestClient);

        logger.info("Create response: " + createResponse);
        UUID basketId = createResponse.getResponseBody();

        logger.info("Querying all items");
        var itemsResponse = getItems(webTestClient);
        logger.info("All items: " + itemsResponse);

        var items = itemsResponse.getResponseBody();
        var firstTestItem = items[0];
        var secondTestItem = items[1];

        logger.info("Try adding invalid items");
        putItem(webTestClient, basketId, firstTestItem.getId(), -2, HttpStatus.UNPROCESSABLE_ENTITY);
        putItem(webTestClient, basketId, UUID.randomUUID(), 2, HttpStatus.NOT_FOUND);
        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems()).isEmpty();

        logger.info("Add some item");
        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems()).containsEntry(firstTestItem.getId(), 5);

        logger.info("Add and update items");
        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .containsEntry(secondTestItem.getId(), 1);
        assertThat(basket.getValue()).isEqualTo(3 * firstTestItem.getPrice() + secondTestItem.getPrice());

        logger.info("Delete item");
        deleteBasketItem(webTestClient, basketId, secondTestItem.getId(), HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .doesNotContainKey(secondTestItem.getId());
        assertThat(basket.getValue()).isEqualTo(3 * firstTestItem.getPrice());

        logger.info("Delete basket");
        deleteBasket(webTestClient, basketId);
    }

    @Test
    public void payTest(@Autowired WebTestClient webTestClient) {
        logger.info("Error message for not existing basket");
        var wrongBasketId = UUID.randomUUID();
        payBasket(webTestClient, wrongBasketId, 3, HttpStatus.NOT_FOUND);

        logger.info("Creating new basket and adding items");
        var createResponse = createBasket(webTestClient);
        UUID basketId = createResponse.getResponseBody();

        logger.info("Paying empty basket is not possible");
        payBasket(webTestClient, basketId, 0, HttpStatus.BAD_REQUEST);

        var itemsResponse = getItems(webTestClient);
        var items = itemsResponse.getResponseBody();

        var firstTestItem = items[0];
        var secondTestItem = items[1];

        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .containsEntry(secondTestItem.getId(), 1);
        assertThat(basket.isPaid()).isFalse();

        logger.info("Paying basket with wrong value not possible");
        payBasket(webTestClient, basketId, basket.getValue() + 1, HttpStatus.BAD_REQUEST);

        logger.info("Paying basket with correct parameters works");
        payBasket(webTestClient, basketId, basket.getValue(), HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.isPaid()).isTrue();

        logger.info("Cannot alter paid basket");
        putItem(webTestClient, basketId, firstTestItem.getId(), 5, HttpStatus.BAD_REQUEST);
        deleteBasketItem(webTestClient, basketId, secondTestItem.getId(), HttpStatus.BAD_REQUEST);
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .containsEntry(secondTestItem.getId(), 1);

        logger.info("Delete basket");
        deleteBasket(webTestClient, basketId);
    }

    @Test
    void redeemTest(@Autowired WebTestClient webTestClient) {
        logger.info("Error message for not existing basket");
        var wrongBasketId = UUID.randomUUID();
        redeemBasket(webTestClient, wrongBasketId, "Some Request", 3, HttpStatus.NOT_FOUND);

        logger.info("Create new basket and adding items");
        var createResponse = createBasket(webTestClient);
        UUID basketId = createResponse.getResponseBody();

        var itemsResponse = getItems(webTestClient);
        var items = itemsResponse.getResponseBody();

        var firstTestItem = items[0];
        var secondTestItem = items[1];

        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        putItem(webTestClient, basketId, secondTestItem.getId(), 1, HttpStatus.OK);

        var basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.getItems())
                .containsEntry(firstTestItem.getId(), 3)
                .containsEntry(secondTestItem.getId(), 1);
        assertThat(basket.isRedeemed()).isFalse();

        logger.info("Redeeming not paid basket not possible");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue(), HttpStatus.BAD_REQUEST);

        logger.info("Pay basket");
        payBasket(webTestClient, basketId, basket.getValue(), HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.isPaid()).isTrue();
        assertThat(basket.isRedeemed()).isFalse();

        logger.info("Redeeming with the wrong redeem value is prohibited");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue() + 1, HttpStatus.BAD_REQUEST);

        logger.info("Payed basket can be redeemed");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue(), HttpStatus.OK);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.isRedeemed()).isTrue();

        logger.info("Redeeming with the wrong redeem value is prohibited");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue() + 1, HttpStatus.BAD_REQUEST);

        logger.info("Re-redeeming with the same request works");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue(), HttpStatus.OK);

        logger.info("Re-redeeming with another request is prohibited");
        redeemBasket(webTestClient, basketId, "Another Request", basket.getValue(), HttpStatus.BAD_REQUEST);

        logger.info("Delete basket");
        deleteBasket(webTestClient, basketId);
    }
}
