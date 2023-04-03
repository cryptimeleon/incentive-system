package org.cryptimeleon.incentive.services.provider;


import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.TestSuiteWithPromotion;
import org.cryptimeleon.incentive.services.provider.repository.CryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.services.provider.ClientHelper.*;

/**
 * Tests for promotion management functionality of incentive service.
 * This includes adding and deleting promotions, listing all existing promotions
 * and assuring that promotion list does not contain duplicates.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PromotionTest {
    // hard-coded promotions used for tests
    private final Promotion firstTestPromotion = TestSuiteWithPromotion.promotion;
    private final Promotion secondTestPromotion = TestSuiteWithPromotion.alternativePromotion;

    /*
     * Declares the crypto repository field as an attribute that is mocked
     * (i.e. replaced by an object providing the same API but possibly with different implementation).
     * This is done to prevent the crypto repository from attempting to connect to the info service
     * which allows testing in environments where no info service is running.
     */
    @MockBean
    private CryptoRepository cryptoRepository;

    // shared secret used to make authenticated requests to the promotion service
    @Value("${incentive-service.provider-secret}")
    private String providerSecret;

    /**
     * Deletes all promotions that currently exist in the system.
     * Annotation leads to this being executed before every single test
     * to ensure a fresh + well-defined test scenario that is independent of previous tests.
     *
     * @param webTestClient test client used to make the DELETE request
     */
    @BeforeEach
    public void clearPromotionRepository(@Autowired WebTestClient webTestClient) {
        deleteAllPromotions(webTestClient, providerSecret, HttpStatus.OK);
    }

    @Test
    public void addPromotionTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, firstTestPromotion, providerSecret, HttpStatus.OK);
        addPromotion(webTestClient, secondTestPromotion, providerSecret, HttpStatus.OK);
        var promotions = getPromotions(webTestClient);

        assertThat(promotions).containsExactly(firstTestPromotion, secondTestPromotion);
    }

    @Test
    public void addPromotionAuthenticationTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, firstTestPromotion, "", HttpStatus.UNAUTHORIZED);
        var promotions = getPromotions(webTestClient);

        assertThat(promotions).isEmpty();
    }

    @Test
    public void addPromotionNoDuplicateTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, firstTestPromotion, providerSecret, HttpStatus.OK);
        addPromotion(webTestClient, firstTestPromotion, providerSecret, HttpStatus.BAD_REQUEST);
        var promotions = getPromotions(webTestClient);

        assertThat(promotions).hasSize(1);
    }

    @Test
    public void deleteAllTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, firstTestPromotion, providerSecret, HttpStatus.OK);
        addPromotion(webTestClient, secondTestPromotion, providerSecret, HttpStatus.OK);

        deleteAllPromotions(webTestClient, providerSecret, HttpStatus.OK);
        var promotionsAfterDelete = getPromotions(webTestClient);

        assertThat(promotionsAfterDelete).isEmpty();
    }

    @Test
    public void deleteAllAuthenticationTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, secondTestPromotion, providerSecret, HttpStatus.OK);

        deleteAllPromotions(webTestClient, "", HttpStatus.UNAUTHORIZED);
        var promotionsAfterDelete = getPromotions(webTestClient);

        assertThat(promotionsAfterDelete).hasSize(1).containsExactly(secondTestPromotion);
    }
}
