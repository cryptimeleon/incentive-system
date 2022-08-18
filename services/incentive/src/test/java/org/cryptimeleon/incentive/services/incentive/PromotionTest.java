package org.cryptimeleon.incentive.services.incentive;


import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.streak.StreakPromotion;
import org.cryptimeleon.incentive.services.incentive.repository.CryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.services.incentive.ClientHelper.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PromotionTest {

    private final Promotion firstTestPromotion = new HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "First Test Promotion",
            "First Test Description",
            List.of(new HazelTokenUpdate(UUID.randomUUID(), "Reward", new RewardSideEffect("Yay"), 2)),
            "Test");
    private final Promotion secondTestPromotion = new StreakPromotion(
            HazelPromotion.generatePromotionParameters(),
            "Second Test Promotion",
            "Second Test Description",
            List.of(new HazelTokenUpdate(UUID.randomUUID(), "Reward", new RewardSideEffect("Yay"), 2)),
            7);
    // Do not use real crypto repository since it automatically queries info service
    @MockBean
    private CryptoRepository cryptoRepository;

    @Value("${incentive-service.provider-secret}")
    private String providerSecret;

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
