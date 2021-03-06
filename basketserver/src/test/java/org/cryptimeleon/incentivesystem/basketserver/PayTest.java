package org.cryptimeleon.incentivesystem.basketserver;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentivesystem.basketserver.ClientHelper.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PayTest {

    Logger logger = LoggerFactory.getLogger(BasketController.class);

    @Value("${basketserver.pay-secret}")
    private String paymentSecret;

    @Value("${basketserver.redeem-secret}")
    private String redeemSecret;

    @Test
    public void payTest(@Autowired WebTestClient webTestClient) {
        logger.info("Error message for not existing basket");
        var wrongBasketId = UUID.randomUUID();
        payBasket(webTestClient, wrongBasketId, 3, HttpStatus.NOT_FOUND, paymentSecret);

        logger.info("Creating new basket and adding items");
        var createResponse = createBasket(webTestClient);
        UUID basketId = createResponse.getResponseBody();

        logger.info("Paying empty basket is not possible");
        payBasket(webTestClient, basketId, 0, HttpStatus.BAD_REQUEST, paymentSecret);

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
        payBasket(webTestClient, basketId, basket.getValue() + 1, HttpStatus.BAD_REQUEST, paymentSecret);

        logger.info("Paying basket with correct parameters works");
        payBasket(webTestClient, basketId, basket.getValue(), HttpStatus.OK, paymentSecret);
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
        redeemBasket(webTestClient, wrongBasketId, "Some Request", 3, HttpStatus.NOT_FOUND, redeemSecret);

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
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue(), HttpStatus.BAD_REQUEST, redeemSecret);

        logger.info("Pay basket");
        payBasket(webTestClient, basketId, basket.getValue(), HttpStatus.OK, paymentSecret);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.isPaid()).isTrue();
        assertThat(basket.isRedeemed()).isFalse();

        logger.info("Redeeming with the wrong redeem value is prohibited");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue() + 1, HttpStatus.BAD_REQUEST, redeemSecret);

        logger.info("Payed basket can be redeemed");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue(), HttpStatus.OK, redeemSecret);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.isRedeemed()).isTrue();

        logger.info("Redeeming with the wrong redeem value is prohibited");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue() + 1, HttpStatus.BAD_REQUEST, redeemSecret);

        logger.info("Re-redeeming with the same request works");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue(), HttpStatus.OK, redeemSecret);

        logger.info("Re-redeeming with another request is prohibited");
        redeemBasket(webTestClient, basketId, "Another Request", basket.getValue(), HttpStatus.BAD_REQUEST, redeemSecret);

        logger.info("Delete basket");
        deleteBasket(webTestClient, basketId);
    }
}
