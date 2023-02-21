package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.client.dto.store.BulkRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.BulkResultsStoreDto;
import org.cryptimeleon.incentive.client.dto.store.EarnRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.SpendRequestStoreDto;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.promotion.TestSuiteWithPromotion;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.incentive.services.basket.repository.DsidBlacklistRepository;
import org.cryptimeleon.incentive.services.basket.repository.PromotionRepository;
import org.cryptimeleon.incentive.services.basket.storage.*;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cryptimeleon.incentive.crypto.TestSuite.generateToken;
import static org.cryptimeleon.incentive.crypto.TestSuite.incentiveSystem;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StoreTest {
    private final BasketEntity basket = constructBasketEntity();
    private final BasketEntity paidBasket = new BasketEntity(TestSuiteWithPromotion.basket.getBasketId(), new HashSet<>(), new HashSet<>(), true, false, true, "", new HashMap<>());
    private final JSONConverter jsonConverter = new JSONConverter();
    @MockBean
    private CryptoRepository cryptoRepository;
    @MockBean
    private BasketRepository basketRepository;
    @MockBean
    private RewardItemRepository rewardItemRepository;
    @MockBean
    private PromotionRepository promotionRepository;
    @MockBean
    private DsidBlacklistRepository dsidBlacklistRepository;
    private Token token;

    private static BasketEntity constructBasketEntity() {
        var b = new BasketEntity(TestSuiteWithPromotion.basket.getBasketId(), new HashSet<>(), new HashSet<>(), false, false, true, "", new HashMap<>());
        b.getBasketItems().addAll(
                TestSuiteWithPromotion.basket.getBasketItemList().stream().map(basketItem -> {
                            var e = new ItemInBasketEntity(b, new ItemEntity(basketItem.getItemId(), basketItem.getTitle(), (long) basketItem.getPrice(), new HashSet<>()));
                            e.setCount(basketItem.getCount());
                            return e;
                        }
                ).collect(Collectors.toList())
        );
        return b;
    }

    @BeforeEach
    public void mock() {
        // program hard-coded return values for the crypto and basket repositories using mockito
        when(cryptoRepository.getPublicParameters()).thenReturn(TestSuiteWithPromotion.pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(TestSuiteWithPromotion.providerKeyPair.getPk());
        when(cryptoRepository.getStorePublicKey()).thenReturn(TestSuiteWithPromotion.storeKeyPair.getPk());
        when(cryptoRepository.getStoreSecretKey()).thenReturn(TestSuiteWithPromotion.storeKeyPair.getSk());
        when(cryptoRepository.getStoreKeyPair()).thenReturn(TestSuiteWithPromotion.storeKeyPair);
        when(basketRepository.findById(TestSuiteWithPromotion.basket.getBasketId())).thenReturn(Optional.of(basket));
        when(promotionRepository.getPromotion(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId()))
                .thenReturn(Optional.of(TestSuiteWithPromotion.promotion));
        var id = ((RewardSideEffect) TestSuiteWithPromotion.spendTokenUpdate.getSideEffect()).getRewardId();
        when(rewardItemRepository.findById(id)).thenReturn(Optional.of(new RewardItemEntity(id, "Test Reward")));

        token = generateToken(TestSuiteWithPromotion.promotion.getPromotionParameters(), TestSuiteWithPromotion.pointsBeforeSpend);
    }

    @Test
    void registerUserTest(@Autowired WebTestClient webTestClient) {
        String userInfo = "Some Test User";

        String serializedRegistrationCoupon = getRegisterResponseSpec(webTestClient, userInfo, TestSuite.userKeyPair.getPk())
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        RegistrationCoupon registrationCoupon = new RegistrationCoupon(jsonConverter.deserialize(serializedRegistrationCoupon), TestSuiteWithPromotion.incentiveSystemRestorer);

        assertThat(incentiveSystem.verifyRegistrationCoupon(registrationCoupon, (s) -> true)).isTrue();
    }

    @Test
    void earnPointsTest(@Autowired WebTestClient webTestClient) {
        EarnStoreRequest earnStoreRequest = incentiveSystem.generateEarnCouponRequest(token, TestSuiteWithPromotion.userKeyPair);
        BulkRequestStoreDto bulkRequestStoreDto = new BulkRequestStoreDto(TestSuiteWithPromotion.basket.getBasketId(),
                List.of(new EarnRequestStoreDto(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(earnStoreRequest.getRepresentation()))),
                Collections.emptyList());

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);

        // Not paid yet
        getBulkResponseSpec(webTestClient, TestSuiteWithPromotion.basket.getBasketId())
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);

        // Paid
        when(basketRepository.findById(TestSuiteWithPromotion.basket.getBasketId())).thenReturn(Optional.of(paidBasket));
        BulkResultsStoreDto bulkResultsStoreDto = getBulkResponseSpec(webTestClient, TestSuiteWithPromotion.basket.getBasketId())
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(BulkResultsStoreDto.class)
                .returnResult()
                .getResponseBody();

        assert bulkResultsStoreDto != null;
        assertThat(bulkResultsStoreDto.getEarnResults()).hasSize(1);
        assertThat(bulkResultsStoreDto.getSpendResults()).isEmpty();


        assertThat(bulkResultsStoreDto.getEarnResults().get(0).getPromotionId()).isEqualTo(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId());
        String serializedStoreEarnCoupon = bulkResultsStoreDto.getEarnResults().get(0).getSerializedEarnCouponSignature();
        EarnStoreCouponSignature earnStoreCouponSignature = new EarnStoreCouponSignature(jsonConverter.deserialize(serializedStoreEarnCoupon));

        Vector<BigInteger> deltaK = TestSuiteWithPromotion.promotion.computeEarningsForBasket(StoreService.promotionBasketFromBasketEntity(basket));
        assertThat(incentiveSystem.verifyEarnCoupon(
                        earnStoreRequest,
                        TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId(),
                        deltaK,
                        earnStoreCouponSignature,
                        storePublicKey -> true
                )
        ).isTrue();
    }

    @Test
    void earnPointRetryTest(@Autowired WebTestClient webTestClient) {
        EarnStoreRequest earnStoreRequest = incentiveSystem.generateEarnCouponRequest(token, TestSuite.userKeyPair);
        BulkRequestStoreDto bulkRequestStoreDto = new BulkRequestStoreDto(TestSuiteWithPromotion.basket.getBasketId(),
                List.of(new EarnRequestStoreDto(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(earnStoreRequest.getRepresentation()))),
                Collections.emptyList());

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);

        EarnStoreRequest secondEarnStoreRequest = incentiveSystem.generateEarnCouponRequest(token, TestSuite.userKeyPair);
        BulkRequestStoreDto secondBulkRequestStoreDto = new BulkRequestStoreDto(TestSuiteWithPromotion.basket.getBasketId(),
                List.of(new EarnRequestStoreDto(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(secondEarnStoreRequest.getRepresentation()))),
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
        EarnStoreRequest earnStoreRequest = incentiveSystem.generateEarnCouponRequest(token, TestSuite.userKeyPair);
        BulkRequestStoreDto bulkRequestStoreDto = new BulkRequestStoreDto(TestSuiteWithPromotion.basket.getBasketId(),
                List.of(new EarnRequestStoreDto(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(earnStoreRequest.getRepresentation()))),
                Collections.emptyList());

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);

        Token secondToken = generateToken(TestSuiteWithPromotion.promotion.getPromotionParameters());
        EarnStoreRequest secondEarnStoreRequest = incentiveSystem.generateEarnCouponRequest(secondToken, TestSuite.userKeyPair);
        BulkRequestStoreDto secondBulkRequestStoreDto = new BulkRequestStoreDto(TestSuiteWithPromotion.basket.getBasketId(),
                List.of(new EarnRequestStoreDto(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(secondEarnStoreRequest.getRepresentation()))),
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
        SpendCouponRequest spendCouponRequest = generateSpendCouponRequest(token);

        BulkRequestStoreDto bulkRequestStoreDto = generateBulkStoreDto(spendCouponRequest);

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);

        // Check that reward items are added to basket
        assertThat(basket.getRewardItems()).hasSize(1);

        // Not paid yet
        getBulkResponseSpec(webTestClient, TestSuiteWithPromotion.basket.getBasketId())
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);

        // Paid
        when(basketRepository.findById(TestSuiteWithPromotion.basket.getBasketId())).thenReturn(Optional.of(paidBasket));
        BulkResultsStoreDto bulkResultsStoreDto = getBulkResponseSpec(webTestClient, TestSuiteWithPromotion.basket.getBasketId())
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(BulkResultsStoreDto.class)
                .returnResult()
                .getResponseBody();

        assert bulkResultsStoreDto != null;
        assertThat(bulkResultsStoreDto.getEarnResults()).isEmpty();
        assertThat(bulkResultsStoreDto.getSpendResults()).hasSize(1);
        assertThat(bulkResultsStoreDto.getSpendResults().get(0).getPromotionId()).isEqualTo(TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId());

        SpendCouponSignature spendCouponSignature = new SpendCouponSignature(jsonConverter.deserialize(bulkResultsStoreDto.getSpendResults().get(0).getSerializedSpendCouponSignature()));
        assertThat(incentiveSystem.verifySpendCouponSignature(spendCouponRequest, spendCouponSignature, TestSuiteWithPromotion.promotion.getPromotionParameters(), TestSuiteWithPromotion.basket.getBasketId()))
                .isTrue();
    }

    @Test
    void spendPointsRetryTest(@Autowired WebTestClient webTestClient) {
        SpendCouponRequest spendCouponRequest = generateSpendCouponRequest(token);

        BulkRequestStoreDto bulkRequestStoreDto = generateBulkStoreDto(spendCouponRequest);

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

        SpendCouponRequest secondSpendCouponRequest = generateSpendCouponRequest(token);

        BulkRequestStoreDto secondBulkRequestStoreDto = generateBulkStoreDto(secondSpendCouponRequest);

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
        SpendCouponRequest spendCouponRequest = generateSpendCouponRequest(token);

        BulkRequestStoreDto bulkRequestStoreDto = generateBulkStoreDto(spendCouponRequest);

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);

        Token secondToken = generateToken(TestSuiteWithPromotion.promotion.getPromotionParameters(), TestSuiteWithPromotion.pointsBeforeSpend);
        SpendCouponRequest secondSpendCouponRequest = generateSpendCouponRequest(secondToken);

        BulkRequestStoreDto secondBulkRequestStoreDto = generateBulkStoreDto(secondSpendCouponRequest);

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
        SpendCouponRequest spendCouponRequest = generateSpendCouponRequest(token);

        BulkRequestStoreDto bulkRequestStoreDto = generateBulkStoreDto(spendCouponRequest);

        webTestClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestStoreDto))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR); // TODO improve error handling, BAD_REQUEST instead
    }

    private BulkRequestStoreDto generateBulkStoreDto(SpendCouponRequest spendCouponRequest) {
        var spendRequestDto = new SpendRequestStoreDto(
                jsonConverter.serialize(spendCouponRequest.getRepresentation()),
                TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId(),
                TestSuiteWithPromotion.spendTokenUpdateId,
                jsonConverter.serialize(new RepresentableRepresentation(TestSuiteWithPromotion.metadata)));
        return new BulkRequestStoreDto(TestSuiteWithPromotion.basket.getBasketId(),
                Collections.emptyList(),
                List.of(spendRequestDto)
        );
    }

    private SpendCouponRequest generateSpendCouponRequest(Token token) {
        return incentiveSystem.generateStoreSpendRequest(
                TestSuiteWithPromotion.userKeyPair,
                TestSuiteWithPromotion.providerKeyPair.getPk(),
                token,
                TestSuiteWithPromotion.promotion.getPromotionParameters(),
                TestSuiteWithPromotion.basket.getBasketId(),
                TestSuiteWithPromotion.pointsAfterSpend,
                TestSuiteWithPromotion.tree,
                TestSuiteWithPromotion.context);
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
