package org.cryptimeleon.incentive.services.basket;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SmokeTest {

    @Test
    void contextLoads() {
    }

    @Test
    void helloWorldTest(@Autowired WebTestClient webClient) {
        Assertions.assertThat(webClient.get().uri("/").exchange().expectStatus().isOk().expectBody(String.class).returnResult().getResponseBody()).contains("Basket");
    }
}
