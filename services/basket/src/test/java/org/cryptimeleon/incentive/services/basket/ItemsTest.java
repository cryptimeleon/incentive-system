package org.cryptimeleon.incentive.services.basket;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.services.basket.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.services.basket.ClientHelper.getItems;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemsTest {
    @Value("${basket-service.provider-secret}")
    private String providerSecret;

    private final Item firstTestItem = new Item("23578", "First test item", 235);
    private final Item secondTestItem = new Item("1234554", "Second test item", 123);

    @BeforeEach
    void removeAllItems(@Autowired WebTestClient webTestClient) {
        ClientHelper.deleteAllItems(webTestClient, providerSecret, HttpStatus.OK);
    }

    @Test
    void addItemsTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.newItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);
        ClientHelper.newItem(webTestClient, secondTestItem, providerSecret, HttpStatus.OK);

        var itemsResponse = getItems(webTestClient).getResponseBody();

        assertThat(itemsResponse).
                contains(firstTestItem, secondTestItem).
                hasSize(2);
    }

    @Test
    void addItemsNoDuplicateTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.newItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);
        ClientHelper.newItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);

        var itemsResponse = getItems(webTestClient).getResponseBody();

        assertThat(itemsResponse).
                contains(firstTestItem).
                hasSize(1);
    }

    @Test
    void deleteAllItemsTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.newItem(webTestClient, firstTestItem, providerSecret, HttpStatus.OK);

        ClientHelper.deleteAllItems(webTestClient, providerSecret, HttpStatus.OK);
        var itemsResponse = getItems(webTestClient).getResponseBody();

        assertThat(itemsResponse).hasSize(0);
    }

    @Test
    void addItemsAuthorizationTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.newItem(webTestClient, firstTestItem, "", HttpStatus.UNAUTHORIZED);
        var itemsResponse = getItems(webTestClient).getResponseBody();

        assertThat(itemsResponse).hasSize(0);
    }

    @Test
    void deleteAllItemsAuthorizationTest(@Autowired WebTestClient webTestClient) {
        ClientHelper.deleteAllItems(webTestClient, "", HttpStatus.UNAUTHORIZED);
        var itemsResponse = getItems(webTestClient).getResponseBody();

        assertThat(itemsResponse).hasSize(0);
    }

    @Test
    void getItemNonexistentIdTest(@Autowired WebTestClient webTestClient) {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/items/{id}").build("12341234123"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
