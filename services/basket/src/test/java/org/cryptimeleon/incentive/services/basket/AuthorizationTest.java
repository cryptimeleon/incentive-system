package org.cryptimeleon.incentive.services.basket;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.services.basket.ClientHelper.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthorizationTest {

    @Value("${basket-service.pay-secret}")
    private String paymentSecret;

    @Value("${basket-service.redeem-secret}")
    private String redeemSecret;

    @Test
    void authorizationTest(@Autowired WebTestClient webTestClient) {
        var invalidRedeemSecret = redeemSecret + "asdf";
        var invalidPaySecret = redeemSecret + "asdf";

        log.info("Create new basket and add items");
        UUID basketId = createBasket(webTestClient).getResponseBody();
        var items = getItems(webTestClient).getResponseBody();
        var firstTestItem = items[0];
        putItem(webTestClient, basketId, firstTestItem.getId(), 3, HttpStatus.OK);
        var basket = queryBasket(webTestClient, basketId).getResponseBody();

        log.info("Unauthorized pay request results in UNAUTHORIZED");
        payBasket(webTestClient, basketId, basket.getValue(), HttpStatus.UNAUTHORIZED, invalidPaySecret);

        log.info("Payment works with valid secret");
        payBasket(webTestClient, basketId, basket.getValue(), HttpStatus.OK, paymentSecret);

        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.isPaid()).isTrue();

        log.info("Unauthorized redeem request results in UNAUTHORIZED");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue(), HttpStatus.UNAUTHORIZED, invalidRedeemSecret);

        log.info("Redeeming works with valid secret");
        redeemBasket(webTestClient, basketId, "Some Request", basket.getValue(), HttpStatus.OK, redeemSecret);
        basket = queryBasket(webTestClient, basketId).getResponseBody();
        assertThat(basket.isRedeemed()).isTrue();

        log.info("Delete basket");
        deleteBasket(webTestClient, basketId);
    }
}
