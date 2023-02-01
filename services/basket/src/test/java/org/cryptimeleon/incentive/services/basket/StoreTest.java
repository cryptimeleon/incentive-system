package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.EarnStoreCouponSignature;
import org.cryptimeleon.incentive.crypto.model.EarnStoreRequest;
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.TestSuiteWithPromotion;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.incentive.services.basket.repository.PromotionRepository;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;
import org.cryptimeleon.incentive.services.basket.storage.BasketRepository;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.crypto.crypto.TestSuite.incentiveSystem;
import static org.cryptimeleon.incentive.crypto.crypto.TestSuite.userKeyPair;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StoreTest {

    @MockBean
    private CryptoRepository cryptoRepository;
    @MockBean
    private BasketRepository basketRepository;
    @MockBean
    private PromotionRepository promotionRepository;
    private final UUID basketId = UUID.randomUUID();
    private final BasketEntity basket = new BasketEntity(basketId, Collections.emptySet(), Collections.emptySet(), false, false, true, "");
    private final JSONConverter jsonConverter = new JSONConverter();

    @BeforeEach
    public void mock() {
        // program hard-coded return values for the crypto and basket repositories using mockito
        when(cryptoRepository.getPublicParameters()).thenReturn(TestSuiteWithPromotion.pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(TestSuiteWithPromotion.incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(TestSuiteWithPromotion.providerKeyPair.getPk());
        when(cryptoRepository.getStorePublicKey()).thenReturn(TestSuiteWithPromotion.storeKeyPair.getPk());
        when(cryptoRepository.getStoreSecretKey()).thenReturn(TestSuiteWithPromotion.storeKeyPair.getSk());
        when(cryptoRepository.getStoreKeyPair()).thenReturn(TestSuiteWithPromotion.storeKeyPair);
        when(basketRepository.findById(basketId)).thenReturn(Optional.of(basket));
        when(promotionRepository.getPromotion(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId()))
                .thenReturn(Optional.of(TestSuiteWithPromotion.promotion));
    }

    @Test
    void registerUserTest(@Autowired WebTestClient webTestClient) {
        UserKeyPair userKeyPair = TestSuite.userKeyPair;
        JSONConverter jsonConverter = new JSONConverter();
        String userInfo = "Some Test User";

        String serializedRegistrationCoupon = webTestClient.get()
                .uri("/register-user-and-obtain-serialized-registration-coupon")
                .header("user-public-key", jsonConverter.serialize(userKeyPair.getPk().getRepresentation()))
                .header("user-info", userInfo)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        RegistrationCoupon registrationCoupon = new RegistrationCoupon(jsonConverter.deserialize(serializedRegistrationCoupon), TestSuiteWithPromotion.incentiveSystemRestorer);

        assertThat(TestSuiteWithPromotion.incentiveSystem.verifyRegistrationCoupon(registrationCoupon, (s) -> true)).isTrue();
    }

    @Test
    void earnPointsTest(@Autowired WebTestClient webTestClient) {
        Promotion promotion = TestSuiteWithPromotion.promotion;
        Token token = TestSuiteWithPromotion.generateToken(promotion.getPromotionParameters());
        // TODO implement and test store side locking mechanism
        EarnStoreRequest earnStoreRequest = incentiveSystem.generateEarnCouponRequest(token, userKeyPair, basketId, promotion.getPromotionParameters().getPromotionId());


        String serializedStoreEarnCoupon = webTestClient.get()
                .uri("/earn")
                .header("earn-store-request", jsonConverter.serialize(earnStoreRequest.getRepresentation()))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        EarnStoreCouponSignature earnStoreCouponSignature = new EarnStoreCouponSignature(jsonConverter.deserialize(serializedStoreEarnCoupon));

        Vector<BigInteger> deltaK = promotion.computeEarningsForBasket(StoreService.promotionBasketFromBasketEntity(basket));
        assertThat(TestSuiteWithPromotion.incentiveSystem.verifyEarnCoupon(
                earnStoreRequest, deltaK,
                earnStoreCouponSignature,
                        storePublicKey -> true
                )
        ).isTrue();
    }
}
