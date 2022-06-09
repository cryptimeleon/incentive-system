package org.cryptimeleon.incentive.services.basket;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.services.basket.model.RewardItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.services.basket.ClientHelper.getRewards;
import static org.cryptimeleon.incentive.services.basket.ClientHelper.newRewardItem;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RewardItemsTest {

    @Value("${basket-service.provider-secret}")
    private String providerSecret;

    private final RewardItem firstTestItem =
            new RewardItem("0580082614202", "Hazelnut Spread");
    private final RewardItem secondTestItem =
            new RewardItem("4499722672546", "Large Hazelnut Spread");

    @BeforeEach
    void removeAllRewardItems(@Autowired WebTestClient webTestClient) {
        ClientHelper.deleteAllRewardItems(webTestClient, providerSecret, HttpStatus.OK);
    }

    @Test
    void addItemsTest(@Autowired WebTestClient webTestClient) {
        newRewardItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);
        newRewardItem(webTestClient, secondTestItem, providerSecret, HttpStatus.OK);

        var itemsResponse = getRewards(webTestClient).getResponseBody();

        assertThat(itemsResponse).
                contains(firstTestItem, secondTestItem).
                hasSize(2);
    }

    @Test
    void addItemsNoDuplicateTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.newRewardItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);
        ClientHelper.newRewardItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);

        var itemsResponse = getRewards(webTestClient).getResponseBody();

        assertThat(itemsResponse).
                contains(firstTestItem).
                hasSize(1);
    }

    @Test
    void deleteAllItemsTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.newRewardItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);

        ClientHelper.deleteAllRewardItems(webTestClient, providerSecret, HttpStatus.OK);
        var itemsResponse = getRewards(webTestClient).getResponseBody();

        assertThat(itemsResponse).hasSize(0);
    }

    @Test
    void addItemsAuthorizationTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.newRewardItem(webTestClient, firstTestItem, "", HttpStatus.UNAUTHORIZED);
        var itemsResponse = getRewards(webTestClient).getResponseBody();

        assertThat(itemsResponse).hasSize(0);
    }

    @Test
    void deleteAllItemsAuthorizationTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.deleteAllItems(webTestClient, "", HttpStatus.UNAUTHORIZED);
        var itemsResponse = getRewards(webTestClient).getResponseBody();

        assertThat(itemsResponse).hasSize(0);
    }
}
