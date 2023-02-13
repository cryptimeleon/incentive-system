package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.client.dto.store.BulkRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.EarnRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.SpendRequestStoreDto;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.EarnStoreRequest;
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.SpendCouponRequest;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.ContextManager;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.TestSuiteWithPromotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.incentive.services.basket.repository.PromotionRepository;
import org.cryptimeleon.incentive.services.basket.storage.BasketEntity;
import org.cryptimeleon.incentive.services.basket.storage.BasketRepository;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
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
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigInteger;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.crypto.TestSuite.*;
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
    private final BasketEntity basket = new BasketEntity(basketId, Collections.emptySet(), Collections.emptySet(), false, false, true, "", new HashMap<>());
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
        EarnStoreRequest earnStoreRequest = incentiveSystem.generateEarnCouponRequest(token, userKeyPair);
        BulkRequestStoreDto bulkRequestStoreDto = new BulkRequestStoreDto(basketId,
                List.of(new EarnRequestStoreDto(promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(earnStoreRequest.getRepresentation()))),
                Collections.emptyList());

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);

        /*
        String serializedStoreEarnCoupon = ""; // TODO obtain results after payment
        EarnStoreCouponSignature earnStoreCouponSignature = new EarnStoreCouponSignature(jsonConverter.deserialize(serializedStoreEarnCoupon));

        Vector<BigInteger> deltaK = promotion.computeEarningsForBasket(StoreService.promotionBasketFromBasketEntity(basket));
        assertThat(TestSuiteWithPromotion.incentiveSystem.verifyEarnCoupon(
                earnStoreRequest,
                promotion.getPromotionParameters().getPromotionId(),
                deltaK,
                earnStoreCouponSignature,
                storePublicKey -> true
                )
        ).isTrue();
        */
    }

    @Test
    void spendPointsTest(@Autowired WebTestClient webTestClient) {
        Promotion promotion = TestSuiteWithPromotion.promotion;
        Token token = TestSuiteWithPromotion.generateToken(promotion.getPromotionParameters(), TestSuiteWithPromotion.pointsBeforeSpend);
        // TODO implement and test store side locking mechanism

        ZkpTokenUpdateMetadata metadata = TestSuiteWithPromotion.promotion.generateMetadataForUpdate();
        SpendDeductTree tree = TestSuiteWithPromotion.spendTokenUpdate.generateRelationTree(Vector.of(BigInteger.ZERO), metadata);
        UniqueByteRepresentable context = ContextManager.computeContext(TestSuiteWithPromotion.spendTokenUpdateId, metadata);
        SpendCouponRequest spendCouponRequest = incentiveSystem.generateStoreSpendRequest(
                userKeyPair,
                providerKeyPair.getPk(),
                token,
                promotion.getPromotionParameters(),
                basketId,
                TestSuiteWithPromotion.pointsAfterSpend,
                tree,
                context);


        BulkRequestStoreDto bulkRequestStoreDto = new BulkRequestStoreDto(basketId,
                Collections.emptyList(),
                List.of(new SpendRequestStoreDto(jsonConverter.serialize(spendCouponRequest.getRepresentation()), promotion.getPromotionParameters().getPromotionId(), TestSuiteWithPromotion.spendTokenUpdateId, jsonConverter.serialize(new RepresentableRepresentation(metadata))))
        );

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);
    }
}
