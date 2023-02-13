package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.client.dto.store.BulkRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.BulkResultsStoreDto;
import org.cryptimeleon.incentive.client.dto.store.EarnRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.SpendRequestStoreDto;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.ContextManager;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.TestSuiteWithPromotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.incentive.services.basket.repository.DsidBlacklistRepository;
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
import org.mockito.Mockito;
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

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StoreTest {
    private final UUID basketId = UUID.randomUUID();
    private final BasketEntity basket = new BasketEntity(basketId, Collections.emptySet(), Collections.emptySet(), false, false, true, "", new HashMap<>());
    private final BasketEntity paidBasket = new BasketEntity(basketId, Collections.emptySet(), Collections.emptySet(), true, false, true, "", new HashMap<>());
    private final JSONConverter jsonConverter = new JSONConverter();
    private final Promotion promotion = TestSuiteWithPromotion.promotion;
    private final UserKeyPair userKeyPair = TestSuite.userKeyPair;
    @MockBean
    private CryptoRepository cryptoRepository;
    @MockBean
    private BasketRepository basketRepository;
    @MockBean
    private PromotionRepository promotionRepository;
    @MockBean
    private DsidBlacklistRepository dsidBlacklistRepository;
    private Token token;
    private ZkpTokenUpdateMetadata metadata;
    private SpendDeductTree tree;
    private UniqueByteRepresentable context;

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

        token = TestSuiteWithPromotion.generateToken(promotion.getPromotionParameters(), TestSuiteWithPromotion.pointsBeforeSpend);

        metadata = TestSuiteWithPromotion.promotion.generateMetadataForUpdate();
        tree = TestSuiteWithPromotion.spendTokenUpdate.generateRelationTree(Vector.of(BigInteger.ZERO), metadata);
        context = ContextManager.computeContext(TestSuiteWithPromotion.spendTokenUpdateId, metadata);
    }

    @Test
    void registerUserTest(@Autowired WebTestClient webTestClient) {
        String userInfo = "Some Test User";

        String serializedRegistrationCoupon = getRegisterResponseSpec(webTestClient, userInfo, userKeyPair.getPk())
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

        // Not paid yet
        getBulkResponseSpec(webTestClient, basketId)
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);

        // Paid
        when(basketRepository.findById(basketId)).thenReturn(Optional.of(paidBasket));
        BulkResultsStoreDto bulkResultsStoreDto = getBulkResponseSpec(webTestClient, basketId)
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(BulkResultsStoreDto.class)
                .returnResult()
                .getResponseBody();

        assert bulkResultsStoreDto != null;
        assertThat(bulkResultsStoreDto.getEarnResults()).hasSize(1);
        assertThat(bulkResultsStoreDto.getSpendResults()).isEmpty();


        assertThat(bulkResultsStoreDto.getEarnResults().get(0).getPromotionId()).isEqualTo(promotion.getPromotionParameters().getPromotionId());
        String serializedStoreEarnCoupon = bulkResultsStoreDto.getEarnResults().get(0).getSerializedEarnCouponSignature();
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
    }

    @Test
    void earnPointRetryTest(@Autowired WebTestClient webTestClient) {
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

        EarnStoreRequest secondEarnStoreRequest = incentiveSystem.generateEarnCouponRequest(token, userKeyPair);
        BulkRequestStoreDto secondBulkRequestStoreDto = new BulkRequestStoreDto(basketId,
                List.of(new EarnRequestStoreDto(promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(secondEarnStoreRequest.getRepresentation()))),
                Collections.emptyList());

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(secondBulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void earnPointRetryWithDifferentTokenProhibitedTest(@Autowired WebTestClient webTestClient) {
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

        Token secondToken = TestSuite.generateToken(promotion.getPromotionParameters());
        EarnStoreRequest secondEarnStoreRequest = incentiveSystem.generateEarnCouponRequest(secondToken, userKeyPair);
        BulkRequestStoreDto secondBulkRequestStoreDto = new BulkRequestStoreDto(basketId,
                List.of(new EarnRequestStoreDto(promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(secondEarnStoreRequest.getRepresentation()))),
                Collections.emptyList());

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(secondBulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR); // TODO: need to improve error handling! IncentiveSystemError => Bad Request!
    }

    @Test
    void spendPointsTest(@Autowired WebTestClient webTestClient) {
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

        // Not paid yet
        getBulkResponseSpec(webTestClient, basketId)
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);

        // Paid
        when(basketRepository.findById(basketId)).thenReturn(Optional.of(paidBasket));
        BulkResultsStoreDto bulkResultsStoreDto = getBulkResponseSpec(webTestClient, basketId)
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(BulkResultsStoreDto.class)
                .returnResult()
                .getResponseBody();

        assert bulkResultsStoreDto != null;
        assertThat(bulkResultsStoreDto.getEarnResults()).isEmpty();
        assertThat(bulkResultsStoreDto.getSpendResults()).hasSize(1);
        assertThat(bulkResultsStoreDto.getSpendResults().get(0).getPromotionId()).isEqualTo(promotion.getPromotionParameters().getPromotionId());

        SpendCouponSignature spendCouponSignature = new SpendCouponSignature(jsonConverter.deserialize(bulkResultsStoreDto.getSpendResults().get(0).getSerializedSpendCouponSignature()));
        assertThat(incentiveSystem.verifySpendCouponSignature(spendCouponRequest, spendCouponSignature, promotion.getPromotionParameters(), basketId))
                .isTrue();
    }

    @Test
    void spendPointsRetryTest(@Autowired WebTestClient webTestClient) {
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

        // First retry with same request
        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);

        SpendCouponRequest secondSpendCouponRequest = incentiveSystem.generateStoreSpendRequest(
                userKeyPair,
                providerKeyPair.getPk(),
                token,
                promotion.getPromotionParameters(),
                basketId,
                TestSuiteWithPromotion.pointsAfterSpend,
                tree,
                context);

        BulkRequestStoreDto secondBulkRequestStoreDto = new BulkRequestStoreDto(basketId,
                Collections.emptyList(),
                List.of(new SpendRequestStoreDto(jsonConverter.serialize(secondSpendCouponRequest.getRepresentation()), promotion.getPromotionParameters().getPromotionId(), TestSuiteWithPromotion.spendTokenUpdateId, jsonConverter.serialize(new RepresentableRepresentation(metadata))))
        );

        // First retry with different but equivalent request
        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(secondBulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);
    }

    // Some 'spend'-token updates might result in tokens being worth more than before.
    // Hence, there might be an incentive to 'spend' two tokens for one basket.
    @Test
    void spendPointsRetryWithDifferentTokenTest(@Autowired WebTestClient webTestClient) {
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

        Token secondToken = generateToken(promotion.getPromotionParameters(), TestSuiteWithPromotion.pointsBeforeSpend);
        SpendCouponRequest secondSpendCouponRequest = incentiveSystem.generateStoreSpendRequest(
                userKeyPair,
                providerKeyPair.getPk(),
                secondToken,
                promotion.getPromotionParameters(),
                basketId,
                TestSuiteWithPromotion.pointsAfterSpend,
                tree,
                context);

        BulkRequestStoreDto secondBulkRequestStoreDto = new BulkRequestStoreDto(basketId,
                Collections.emptyList(),
                List.of(new SpendRequestStoreDto(jsonConverter.serialize(secondSpendCouponRequest.getRepresentation()), promotion.getPromotionParameters().getPromotionId(), TestSuiteWithPromotion.spendTokenUpdateId, jsonConverter.serialize(new RepresentableRepresentation(metadata))))
        );

        // First retry with different but equivalent request
        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(secondBulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR); // TODO improve error handling, BAD_REQUEST instead
    }

    @Test
    void spendPointsBlacklistedDsidTest(@Autowired WebTestClient webTestClient) {
        when(dsidBlacklistRepository.containsDsidWithDifferentGamma(Mockito.eq(token.getDoubleSpendingId()), Mockito.any())).thenReturn(true);
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
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR); // TODO improve error handling, BAD_REQUEST instead
    }

    private WebTestClient.ResponseSpec getRegisterResponseSpec(WebTestClient webTestClient, String userInfo, UserPublicKey pk) {
        return webTestClient.get()
                .uri("/register-user-and-obtain-serialized-registration-coupon")
                .header("user-public-key", jsonConverter.serialize(pk.getRepresentation()))
                .header("user-info", userInfo)
                .exchange();
    }

    private WebTestClient.ResponseSpec getBulkResponseSpec(WebTestClient webTestClient, UUID basketId) {
        return webTestClient.get()
                .uri("/bulk-results")
                .header("basket-id", String.valueOf(basketId))
                .exchange();
    }
}
