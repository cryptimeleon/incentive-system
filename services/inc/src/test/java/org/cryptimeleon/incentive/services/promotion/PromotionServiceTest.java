package org.cryptimeleon.incentive.services.promotion;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.EarnRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.SpendRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResultsDto;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.services.promotion.repository.BasketRepository;
import org.cryptimeleon.incentive.services.promotion.repository.CryptoRepository;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PromotionServiceTest {

    /**
     * Use a MockBean to prevent the CryptoRepository from being created (and trying to connect to the info service)
     */
    @MockBean
    CryptoRepository cryptoRepository;

    @MockBean
    BasketRepository basketRepository;

    UUID testBasketId = UUID.fromString("f09580eb-a3d6-4646-b6af-03cb0205af5b");
    Basket testBasket = new Basket(testBasketId, List.of(
            new BasketItem(UUID.randomUUID(), "Nutella", 200, 5),
            new BasketItem(UUID.randomUUID(), "Big Nutella", 100, 3)
    ));

    IncentivePublicParameters pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
    IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
    ProviderKeyPair pkp = Setup.providerKeyGen(pp);
    UserKeyPair ukp = Setup.userKeyGen(pp);
    JSONConverter jsonConverter = new JSONConverter();

    Promotion promotionToAdd = new HazelPromotion(HazelPromotion.generatePromotionParameters(),
            "Test Promotion",
            "Test Description",
            List.of(new HazelTokenUpdate(UUID.randomUUID(), "Reward", new RewardSideEffect("Yay"), 2)),
            "Test");

    @BeforeEach
    public void mock() {
        // Setup the mock to return the correct values
        when(cryptoRepository.getPublicParameters()).thenReturn(pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(pkp.getPk());
        when(cryptoRepository.getProviderSecretKey()).thenReturn(pkp.getSk());
        when(basketRepository.getBasket(testBasketId)).thenReturn(testBasket);
    }

    @Test
    public void promotionEndpointTest(@Autowired WebTestClient webClient) {
        List<Promotion> promotions = getPromotions(webClient);

        addPromotion(webClient, promotionToAdd);

        List<Promotion> newPromotions = getPromotions(webClient);

        Assertions.assertEquals(promotions.size() + 1, newPromotions.size());

        // Double insertion fails
        webClient.post()
                .uri("/promotions")
                .body(BodyInserters.fromValue(List.of(jsonConverter.serialize(promotionToAdd.getRepresentation()))))
                .exchange().expectStatus().is4xxClientError();
    }

    private void addPromotion(WebTestClient webClient, Promotion promotionToAdd) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/promotions").build())
                .body(BodyInserters.fromValue(List.of(jsonConverter.serialize(promotionToAdd.getRepresentation()))))
                .exchange().expectStatus().isOk();
    }

    private List<Promotion> getPromotions(@Autowired WebTestClient webClient) {
        String[] newSerializedPromotions = webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/promotions").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String[].class)
                .returnResult().getResponseBody();

        assert newSerializedPromotions != null;
        return Arrays.stream(newSerializedPromotions)
                .map(s -> (Promotion) ((RepresentableRepresentation) jsonConverter.deserialize(s)).recreateRepresentable())
                .collect(Collectors.toList());
    }

    @Test
    public void promotionServiceTest(@Autowired WebTestClient webClient) {

        List<Promotion> promotions = getPromotions(webClient);

        Promotion promotionToJoin = promotions.get(0);

        // Create request to send
        var joinRequest = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp, promotionToJoin.getPromotionParameters());

        // Send request and process response to assert correct behavior
        var serializedJoinResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/join-promotion")
                        .build())
                .header("user-public-key", jsonConverter.serialize(ukp.getPk().getRepresentation()))
                .header("join-request", jsonConverter.serialize(joinRequest.getRepresentation()))
                .header("promotion-id", String.valueOf(promotionToJoin.getPromotionParameters().getPromotionId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), pp);
        Token initialToken = incentiveSystem.handleJoinRequestResponse(promotionToJoin.getPromotionParameters(), pkp.getPk(), ukp, joinRequest, joinResponse);

        // Attempt joining a non-existing promotion id
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/join-promotion")
                        .build())
                .header("user-public-key", jsonConverter.serialize(ukp.getPk().getRepresentation()))
                .header("join-request", jsonConverter.serialize(joinRequest.getRepresentation()))
                .header("promotion-id", String.valueOf(BigInteger.valueOf(42)))
                .exchange()
                .expectStatus()
                .is4xxClientError();

        // Earn
        var pointsToEarn = promotionToJoin.computeEarningsForBasket(testBasket);

        var earnRequest = incentiveSystem.generateEarnRequest(initialToken, pkp.getPk(), ukp);
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-updates").build())
                .header("basket-id", String.valueOf(testBasketId))
                .body(BodyInserters.fromValue(
                        new BulkRequestDto(
                                List.of(new EarnRequestDto(promotionToJoin.getPromotionParameters().getPromotionId(),
                                        jsonConverter.serialize(earnRequest.getRepresentation()))),
                                Collections.emptyList())))
                .exchange()
                .expectStatus()
                .isOk();

        when(basketRepository.isBasketPayed(testBasketId)).thenReturn(true);

        var resultsDto = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-update-results").build())
                .header("basket-id", String.valueOf(testBasketId))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TokenUpdateResultsDto.class)
                .returnResult().getResponseBody();
        var serializedEarnResponse = resultsDto.getEarnTokenUpdateResultDtoList().get(0).getSerializedEarnResponse();
        SPSEQSignature spseqSignature = new SPSEQSignature(jsonConverter.deserialize(serializedEarnResponse), pp.getBg().getG1(), pp.getBg().getG2());

        Token earnedToken = incentiveSystem.handleEarnRequestResponse(promotionToJoin.getPromotionParameters(), earnRequest, spseqSignature, pointsToEarn, initialToken, pkp.getPk(), ukp);

        /* TODO adapt these
        // Earn for non-existing basket
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/earn").build())
                .header("promotion-id", String.valueOf(promotionToJoin.getPromotionParameters().getPromotionId()))
                .header("earn-request", jsonConverter.serialize(earnRequest.getRepresentation()))
                .header("basket-id", String.valueOf(UUID.randomUUID()))
                .exchange()
                .expectStatus()
                .is4xxClientError();

        // Earn for wrong promotion id
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/earn").build())
                .header("promotion-id", String.valueOf(BigInteger.valueOf(3L)))
                .header("earn-request", jsonConverter.serialize(earnRequest.getRepresentation()))
                .header("basket-id", testBasketId.toString())
                .exchange()
                .expectStatus()
                .is4xxClientError();
         */

        // TODO continue from here
        // new basket :)
        when(basketRepository.isBasketPayed(testBasketId)).thenReturn(false);

        // Spend Deduct
        Vector<BigInteger> basketPoints = promotionToJoin.computeEarningsForBasket(testBasket);
        var tokenPoints = new Vector<>(earnedToken.getPoints().map(RingElement::asInteger));
        var possibleRewards = promotionToJoin.computeTokenUpdatesForPoints(tokenPoints, basketPoints, null);
        // User choice in app
        var chosenReward = possibleRewards.get(0);
        var pointsAfterSpend = chosenReward.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).orElseThrow();

        var metadata = promotionToJoin.generateMetadataForUpdate();
        var spendDeductTree = chosenReward.generateRelationTree(basketPoints, metadata);
        var tid = testBasket.getBasketId(pp.getBg().getZn());

        SpendRequest spendRequest = incentiveSystem.generateSpendRequest(promotionToJoin.getPromotionParameters(), earnedToken, pkp.getPk(), pointsAfterSpend, ukp, tid, spendDeductTree);
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-updates").build())
                .header("basket-id", String.valueOf(testBasketId))
                .body(BodyInserters.fromValue(
                        new BulkRequestDto(
                                Collections.emptyList(),
                                List.of(new SpendRequestDto(
                                        promotionToJoin.getPromotionParameters().getPromotionId(),
                                        chosenReward.getTokenUpdateId(),
                                        jsonConverter.serialize(spendRequest.getRepresentation()),
                                        jsonConverter.serialize(new RepresentableRepresentation(metadata))
                                )))))
                .exchange()
                .expectStatus()
                .isOk();

        when(basketRepository.isBasketPayed(testBasketId)).thenReturn(true);

        resultsDto = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-update-results").build())
                .header("basket-id", String.valueOf(testBasketId))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TokenUpdateResultsDto.class)
                .returnResult().getResponseBody();
        var serializedSpendResponse = resultsDto.getZkpTokenUpdateResultDtoList().get(0).getSerializedResponse();

        SpendResponse spendResponse = new SpendResponse(jsonConverter.deserialize(serializedSpendResponse), pp.getBg().getZn(), pp.getSpsEq());
        Token spentToken = incentiveSystem.handleSpendRequestResponse(promotionToJoin.getPromotionParameters(), spendResponse, spendRequest, earnedToken, pointsAfterSpend, pkp.getPk(), ukp);

        /*
        TODO these need to be changed!
        // Some invalid spend requests just to make sure
        // invalid promotion id
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/spend").build())
                .header("promotion-id", String.valueOf(BigInteger.valueOf(42)))
                .header("spend-request", jsonConverter.serialize(spendRequest.getRepresentation()))
                .header("basket-id", testBasketId.toString())
                .header("reward-id", String.valueOf(chosenReward.getTokenUpdateId()))
                .exchange()
                .expectStatus()
                .is4xxClientError();

        // invalid basket id
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/spend").build())
                .header("promotion-id", String.valueOf(promotionToJoin.getPromotionParameters().getPromotionId()))
                .header("spend-request", jsonConverter.serialize(spendRequest.getRepresentation()))
                .header("basket-id", UUID.randomUUID().toString())
                .header("reward-id", String.valueOf(chosenReward.getTokenUpdateId()))
                .exchange()
                .expectStatus()
                .is4xxClientError();

        // invalid reward id
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/spend").build())
                .header("promotion-id", String.valueOf(promotionToJoin.getPromotionParameters().getPromotionId()))
                .header("spend-request", jsonConverter.serialize(spendRequest.getRepresentation()))
                .header("basket-id", testBasketId.toString())
                .header("reward-id", String.valueOf(UUID.randomUUID()))
                .exchange()
                .expectStatus()
                .is4xxClientError();
         */
    }
}
