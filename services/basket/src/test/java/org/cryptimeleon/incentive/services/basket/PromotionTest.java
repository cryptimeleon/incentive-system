package org.cryptimeleon.incentive.services.basket;


import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.TestSuiteWithPromotion;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for promotion management functionality of incentive service.
 * This includes adding and deleting promotions, listing all existing promotions
 * and assuring that promotion list does not contain duplicates.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PromotionTest {
    private static final JSONConverter jsonConverter = new JSONConverter();

    // hard-coded promotions used for tests
    private final Promotion firstTestPromotion = TestSuiteWithPromotion.promotion;
    private final Promotion secondTestPromotion = TestSuiteWithPromotion.alternativePromotion;

    // shared secret used to make authenticated requests to the promotion service
    @Value("${basket-service.provider-secret}")
    private String basketServiceProviderSecret;

    /*
     * Declares the crypto repository field as an attribute that is mocked
     * (i.e. replaced by an object providing the same API but possibly with different implementation).
     * This is done to prevent the crypto repository from attempting to connect to the info service
     * which allows testing in environments where no info service is running.
     */
    @MockBean
    private CryptoRepository cryptoRepository;

    /**
     * Deletes all promotions that currently exist in the system.
     * Annotation leads to this being executed before every single test
     * to ensure a fresh + well-defined test scenario that is independent of previous tests.
     *
     * @param webTestClient test client used to make the DELETE request
     */
    @BeforeEach
    public void clearPromotionRepository(@Autowired WebTestClient webTestClient) {
        deleteAllPromotions(webTestClient, basketServiceProviderSecret, HttpStatus.OK);
    }

    @Test
    public void addPromotionTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, firstTestPromotion, basketServiceProviderSecret, HttpStatus.OK);
        addPromotion(webTestClient, secondTestPromotion, basketServiceProviderSecret, HttpStatus.OK);
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
        addPromotion(webTestClient, firstTestPromotion, basketServiceProviderSecret, HttpStatus.OK);
        addPromotion(webTestClient, firstTestPromotion, basketServiceProviderSecret, HttpStatus.BAD_REQUEST);
        var promotions = getPromotions(webTestClient);

        assertThat(promotions).hasSize(1);
    }

    @Test
    public void deleteAllTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, firstTestPromotion, basketServiceProviderSecret, HttpStatus.OK);
        addPromotion(webTestClient, secondTestPromotion, basketServiceProviderSecret, HttpStatus.OK);

        deleteAllPromotions(webTestClient, basketServiceProviderSecret, HttpStatus.OK);
        var promotionsAfterDelete = getPromotions(webTestClient);

        assertThat(promotionsAfterDelete).isEmpty();
    }

    @Test
    public void deleteAllAuthenticationTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, secondTestPromotion, basketServiceProviderSecret, HttpStatus.OK);

        deleteAllPromotions(webTestClient, "", HttpStatus.UNAUTHORIZED);
        var promotionsAfterDelete = getPromotions(webTestClient);

        assertThat(promotionsAfterDelete).hasSize(1).containsExactly(secondTestPromotion);
    }

    /**
     * Wrapper for request to add-promotion endpoint of incentive server.
     *
     * @param webClient      test client used to make request to server
     * @param promotionToAdd promotion object
     * @param providerSecret shared secret used to authenticate request
     * @param expectedStatus if another HTTP status than this is sent back for the query, an exception is thrown
     */
    static void addPromotion(WebTestClient webClient, Promotion promotionToAdd, String providerSecret, HttpStatus expectedStatus) {
        webClient.post()
                .uri("/promotions")
                .header("store-secret", providerSecret)
                .body(BodyInserters.fromValue(
                        serializePromotionsWithType(promotionToAdd)
                ))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    /**
     * Wrapper around GET request for listing all promotions in the system.
     * Note that this request does not need to be authenticated
     * because it does neither change the server state nor reveals confidential data.
     *
     * @param webClient test client used to make request to the server
     * @return list of promotion objects
     */
    static List<Promotion> getPromotions(@Autowired WebTestClient webClient) {
        // retrieve promotions from server
        String[] newSerializedPromotions = webClient
                .get()
                .uri("/promotions")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String[].class)
                .returnResult()
                .getResponseBody();

        // make sure that formatting is fine (i.e. valid string array is returned)
        assert newSerializedPromotions != null;

        // type conversion
        return Arrays.stream(newSerializedPromotions)
                .map(s -> (Promotion) ((RepresentableRepresentation) jsonConverter.deserialize(s)).recreateRepresentable())
                .collect(Collectors.toList());
    }

    /**
     * Wrapper around DELETE requests for deleting all promotions in the system.
     *
     * @param webTestClient  test client used to make request to the server
     * @param providerSecret shared secret used to authenticate the request
     * @param expectedStatus if another HTTP status than this is sent back for the query, an exception is thrown
     */
    static void deleteAllPromotions(WebTestClient webTestClient, String providerSecret, HttpStatus expectedStatus) {
        webTestClient
                .delete()
                .uri("/promotions")
                .header("store-secret", providerSecret)
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    /**
     * Computes a serialized representation of a promotion object.
     * Used when marshalling a promotion for adding it to the system.
     */
    private static List<String> serializePromotionsWithType(Promotion promotionToAdd) {
        return List.of(jsonConverter.serialize(new RepresentableRepresentation(promotionToAdd)));
    }
}
