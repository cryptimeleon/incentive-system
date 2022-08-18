package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.SpendRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResultsDto;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.services.incentive.repository.BasketRepository;
import org.cryptimeleon.incentive.services.incentive.repository.CryptoRepository;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.cryptimeleon.incentive.services.incentive.ClientHelper.*;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IncentiveServiceTest {


    private static final IncentivePublicParameters pp = TestSuite.pp;
    private static final IncentiveSystem incentiveSystem = TestSuite.incentiveSystem;
    private static final ProviderKeyPair pkp = TestSuite.providerKeyPair;
    private static final UserKeyPair ukp = TestSuite.userKeyPair;
    private static final JSONConverter jsonConverter = new JSONConverter();
    private final Basket testBasket = new Basket(
            UUID.randomUUID(),
            List.of(
                    new BasketItem(UUID.randomUUID().toString(), "Hazelnut Spread", 200, 5),
                    new BasketItem(UUID.randomUUID().toString(), "Large Hazelnut Spread", 100, 3)
            )
    );
    private final Basket emptyTestBasket = new Basket(
            UUID.randomUUID(),
            List.of()
    );
    private final HazelTokenUpdate testTokenUpdate = new HazelTokenUpdate(UUID.randomUUID(),
            "Reward",
            new RewardSideEffect("Yay"),
            2);
    private final Promotion testPromotion = new HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "Test Promotion",
            "Test Description",
            List.of(testTokenUpdate),
            "Test");

    @Value("${incentive-service.provider-secret}")
    private String providerSecret;
    // Use a MockBean to prevent the CryptoRepository from being created (and trying to connect to the info service)
    @MockBean
    private CryptoRepository cryptoRepository;
    // Mock basket repository to inject baskets
    @MockBean
    private BasketRepository basketRepository;

    @BeforeEach
    public void mock(@Autowired WebTestClient webTestClient) {
        // Setup the mock to return the correct values
        when(cryptoRepository.getPublicParameters()).thenReturn(pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(pkp.getPk());
        when(cryptoRepository.getProviderSecretKey()).thenReturn(pkp.getSk());
        when(basketRepository.getBasket(testBasket.getBasketId())).thenReturn(testBasket);
        when(basketRepository.isBasketPaid(testBasket.getBasketId())).thenReturn(false);
        when(basketRepository.getBasket(emptyTestBasket.getBasketId())).thenReturn(emptyTestBasket);
        when(basketRepository.isBasketPaid(emptyTestBasket.getBasketId())).thenReturn(false);

        deleteAllPromotions(webTestClient, providerSecret, HttpStatus.OK);
    }

    @Test
    public void genesisTest(@Autowired WebTestClient webClient) {
        var userPreKeyPair = TestSuite.userPreKeyPair;

        SPSEQSignature signature = retrieveGenesisSignature(webClient, userPreKeyPair);

        assertThat(pp.getSpsEq().verify(pkp.getPk().getGenesisSpsEqPk(), signature, userPreKeyPair.getPk().getUpk(), pp.getW()))
                .isTrue();
    }


    @Test
    public void joinTest(@Autowired WebTestClient webClient) {
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);

        Token token = joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.OK);

        assertThat(token).isNotNull();
    }

    @Test
    public void joinNonExistingPromotionTest(@Autowired WebTestClient webClient) {
        assertThatThrownBy(() -> joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.BAD_REQUEST)).hasStackTraceContaining("Promotion");
    }

    @Test
    public void earnTest(@Autowired WebTestClient webClient) {
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);
        Token token = joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.OK);

        var pointsToEarn = testPromotion.computeEarningsForBasket(testBasket);
        var earnRequest = generateAndSendEarnRequest(webClient,
                incentiveSystem,
                pkp,
                ukp,
                token,
                testPromotion.getPromotionParameters().getPromotionId(),
                testBasket.getBasketId(),
                HttpStatus.OK);
        when(basketRepository.isBasketPaid(testBasket.getBasketId())).thenReturn(true);
        retrieveTokenAfterEarn(webClient,
                incentiveSystem,
                testPromotion,
                pkp,
                ukp,
                token,
                earnRequest,
                testBasket.getBasketId(),
                pointsToEarn,
                HttpStatus.OK);
    }

    @Test
    public void earnWrongBasketTest(@Autowired WebTestClient webClient) {
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);
        Token token = joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.OK);

        generateAndSendEarnRequest(webClient,
                incentiveSystem,
                pkp,
                ukp,
                token,
                testPromotion.getPromotionParameters().getPromotionId(),
                UUID.randomUUID(),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    public void earnInvalidPromotionIdTest(@Autowired WebTestClient webClient) {
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);
        Token token = joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.OK);

        generateAndSendEarnRequest(webClient,
                incentiveSystem,
                pkp,
                ukp,
                token,
                BigInteger.valueOf(14),
                UUID.randomUUID(),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    void spendTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, testPromotion, providerSecret, HttpStatus.OK);
        var tokenPoints = Vector.of(BigInteger.valueOf(35));
        var basketPoints = Vector.of(BigInteger.valueOf(0));
        var pointsAfterSpend = testTokenUpdate.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).orElseThrow();
        var token = Helper.generateToken(
                pp,
                ukp,
                pkp,
                testPromotion.getPromotionParameters(),
                tokenPoints
        );

        SpendRequest spendRequest = sendSingleSpendRequest(webTestClient, basketPoints, pointsAfterSpend, token, HttpStatus.OK);
        when(basketRepository.isBasketPaid(emptyTestBasket.getBasketId())).thenReturn(true);
        retrieveTokenAfterSpend(webTestClient, token, pointsAfterSpend, spendRequest);
    }

    @Test
    void spendWithoutPaymentTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, testPromotion, providerSecret, HttpStatus.OK);
        var tokenPoints = Vector.of(BigInteger.valueOf(35));
        var basketPoints = Vector.of(BigInteger.valueOf(0));
        var pointsAfterSpend = testTokenUpdate.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).orElseThrow();
        var token = Helper.generateToken(
                pp,
                ukp,
                pkp,
                testPromotion.getPromotionParameters(),
                tokenPoints
        );

        sendSingleSpendRequest(webTestClient, basketPoints, pointsAfterSpend, token, HttpStatus.OK);

        // Not paid yet
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-update-results").build())
                .header("basket-id", String.valueOf(emptyTestBasket.getBasketId()))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void emptyBulkRequestTest(@Autowired WebTestClient webTestClient) {
        sendBulkRequests(webTestClient, new BulkRequestDto(List.of(), List.of()), emptyTestBasket, HttpStatus.OK);
    }

    private SPSEQSignature retrieveGenesisSignature(WebTestClient webClient, org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair userPreKeyPair) {
        var serializedSignature = webClient.post()
                .uri("/genesis")
                .header("user-public-key", jsonConverter.serialize(userPreKeyPair.getPk().getUpk().getRepresentation()))
                .exchange()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        return pp.getSpsEq().restoreSignature(jsonConverter.deserialize(serializedSignature));
    }

    private SpendRequest sendSingleSpendRequest(WebTestClient webTestClient, Vector<BigInteger> basketPoints, Vector<BigInteger> pointsAfterSpend, Token token, HttpStatus expectedStatus) {
        var metadata = testPromotion.generateMetadataForUpdate();
        var spendDeductTree = testTokenUpdate.generateRelationTree(basketPoints, metadata);
        var tid = emptyTestBasket.getBasketId(pp.getBg().getZn());
        SpendRequest spendRequest = incentiveSystem.generateSpendRequest(testPromotion.getPromotionParameters(), token, pkp.getPk(), pointsAfterSpend, ukp, tid, spendDeductTree);
        var bulkRequestDto = new BulkRequestDto(
                Collections.emptyList(),
                List.of(new SpendRequestDto(
                        testPromotion.getPromotionParameters().getPromotionId(),
                        testTokenUpdate.getTokenUpdateId(),
                        jsonConverter.serialize(spendRequest.getRepresentation()),
                        jsonConverter.serialize(new RepresentableRepresentation(metadata))
                )));
        sendBulkRequests(webTestClient, bulkRequestDto, emptyTestBasket, expectedStatus);
        return spendRequest;
    }

    private void sendBulkRequests(WebTestClient webTestClient, BulkRequestDto bulkRequestDto, Basket emptyTestBasket, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-updates").build())
                .header("basket-id", String.valueOf(emptyTestBasket.getBasketId()))
                .body(BodyInserters.fromValue(bulkRequestDto))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    private void retrieveTokenAfterSpend(WebTestClient webTestClient, Token token, Vector<BigInteger> pointsAfterSpend, SpendRequest spendRequest) {
        var resultsDto = webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-update-results").build())
                .header("basket-id", String.valueOf(emptyTestBasket.getBasketId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TokenUpdateResultsDto.class)
                .returnResult().getResponseBody();
        assert resultsDto != null;
        var serializedSpendResponse = resultsDto.getZkpTokenUpdateResultDtoList().get(0).getSerializedResponse();
        SpendResponse spendResponse = new SpendResponse(jsonConverter.deserialize(serializedSpendResponse), pp.getBg().getZn(), pp.getSpsEq());
        incentiveSystem.handleSpendRequestResponse(testPromotion.getPromotionParameters(), spendResponse, spendRequest, token, pointsAfterSpend, pkp.getPk(), ukp);
    }
}
