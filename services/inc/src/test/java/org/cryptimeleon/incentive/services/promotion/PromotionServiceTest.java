package org.cryptimeleon.incentive.services.promotion;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.promotions.NutellaPromotion;
import org.cryptimeleon.incentive.promotion.promotions.Promotion;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.RewardSideEffect;
import org.cryptimeleon.incentive.services.promotion.repository.BasketRepository;
import org.cryptimeleon.incentive.services.promotion.repository.CryptoRepository;
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

    UUID testBasketId = UUID.randomUUID();
    Basket testBasket = new Basket(testBasketId, List.of(
            new BasketItem(UUID.randomUUID(), "Nutella", 200, 5),
            new BasketItem(UUID.randomUUID(), "Big Nutella", 100, 3)
    ));

    IncentivePublicParameters pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
    IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
    ProviderKeyPair pkp = Setup.providerKeyGen(pp);
    UserKeyPair ukp = Setup.userKeyGen(pp);
    JSONConverter jsonConverter = new JSONConverter();

    Promotion promotionToAdd = new NutellaPromotion(NutellaPromotion.generatePromotionParameters(),
            "Test Promotion",
            "Test Description",
            List.of(new NutellaReward(2, "Reward", UUID.randomUUID(), new RewardSideEffect("Yay"))),
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
                .map(s -> new NutellaPromotion(jsonConverter.deserialize(s))).collect(Collectors.toList());
    }

    @Test
    public void promotionServiceTest(@Autowired WebTestClient webClient) {

        List<Promotion> promotions = getPromotions(webClient);

        Promotion promotionToJoin = promotions.get(0);

        // Create request to send
        var joinRequest = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp);

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
        var serializedEarnResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/earn").build())
                .header("promotion-id", String.valueOf(promotionToJoin.getPromotionParameters().getPromotionId()))
                .header("earn-request", jsonConverter.serialize(earnRequest.getRepresentation()))
                .header("basket-id", testBasketId.toString())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        SPSEQSignature spseqSignature = new SPSEQSignature(jsonConverter.deserialize(serializedEarnResponse), pp.getBg().getG1(), pp.getBg().getG2());

        Token earnedToken = incentiveSystem.handleEarnRequestResponse(promotionToJoin.getPromotionParameters(), earnRequest, spseqSignature, pointsToEarn, initialToken, pkp.getPk(), ukp);

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

        // Spend Deduct
        Vector<BigInteger> basketPoints = promotionToJoin.computeEarningsForBasket(testBasket);
        var tokenPoitns = new Vector<>(earnedToken.getPoints().map(RingElement::asInteger));
        var possibleRewards = promotionToJoin.computeRewardsForPoints(tokenPoitns, basketPoints);
        // User choice in app
        var chosenReward = possibleRewards.get(0);
        var pointsAfterSpend = chosenReward.computeSatisfyingNewPointsVector(tokenPoitns, basketPoints).orElseThrow();

        var spendDeductTree = chosenReward.generateRelationTree(basketPoints);
        var tid = testBasket.getBasketId(pp.getBg().getZn());

        SpendRequest spendRequest = incentiveSystem.generateSpendRequest(promotionToJoin.getPromotionParameters(), earnedToken, pkp.getPk(), pointsAfterSpend, ukp, tid, spendDeductTree);

        String serializedSpendResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/spend").build())
                .header("promotion-id", String.valueOf(promotionToJoin.getPromotionParameters().getPromotionId()))
                .header("spend-request", jsonConverter.serialize(spendRequest.getRepresentation()))
                .header("basket-id", testBasketId.toString())
                .header("reward-id", String.valueOf(chosenReward.getRewardId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        SpendResponse spendResponse = new SpendResponse(jsonConverter.deserialize(serializedSpendResponse), pp.getBg().getZn(), pp.getSpsEq());
        Token spentToken = incentiveSystem.handleSpendRequestResponse(promotionToJoin.getPromotionParameters(), spendResponse, spendRequest, earnedToken, pointsAfterSpend, pkp.getPk(), ukp);

        // Some invalid spend requests just to make sure
        // invalid promotion id
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/spend").build())
                .header("promotion-id", String.valueOf(BigInteger.valueOf(42)))
                .header("spend-request", jsonConverter.serialize(spendRequest.getRepresentation()))
                .header("basket-id", testBasketId.toString())
                .header("reward-id", String.valueOf(chosenReward.getRewardId()))
                .exchange()
                .expectStatus()
                .is4xxClientError();

        // invalid basket id
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/spend").build())
                .header("promotion-id", String.valueOf(promotionToJoin.getPromotionParameters().getPromotionId()))
                .header("spend-request", jsonConverter.serialize(spendRequest.getRepresentation()))
                .header("basket-id", UUID.randomUUID().toString())
                .header("reward-id", String.valueOf(chosenReward.getRewardId()))
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
    }
}
