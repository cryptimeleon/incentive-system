package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.EarnRequestDto;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test a full (correct) protocol flow.
 */
@Slf4j
public class FullWorkflowTest extends IncentiveSystemIntegrationTest {

    Promotion testPromotion = new HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "Test Promotion",
            "Some Test Promotion",
            List.of(new HazelTokenUpdate(UUID.randomUUID(), "This is a test reward", new RewardSideEffect("Test Reward Sideffect"), 2)),
            "Apple");

    @Test
    void runFullWorkflow() {
        var infoClient = new InfoClient(infoUrl);
        var incentiveClient = new IncentiveClient(incentiveUrl);
        var basketClient = new BasketClient(basketUrl);
        var dsProtectionClient = new DSProtectionClient(dsProtectionUrl);

        log.info("Retrieve data from info service");
        var serializedPublicParameters = infoClient.querySerializedPublicParameters().block(Duration.ofSeconds(1));
        var serializedProviderPublicKey = infoClient.querySerializedProviderPublicKey().block(Duration.ofSeconds(1));

        log.info("Deserialize data and setup incentive system");
        var jsonConverter = new JSONConverter();
        var publicParameters = new IncentivePublicParameters(jsonConverter.deserialize(serializedPublicParameters));
        var providerPublicKey = new ProviderPublicKey(jsonConverter.deserialize(serializedProviderPublicKey),
                publicParameters.getSpsEq(),
                publicParameters.getBg().getG1());
        var incentiveSystem = new IncentiveSystem(publicParameters);
        assertTrue(incentiveClient.addPromotions(List.of(testPromotion), incentiveProviderSecret)
                .block(Duration.ofSeconds(1))
                .getStatusCode()
                .is2xxSuccessful());

        var userKeyPair = incentiveSystem.generateUserKeys();

        log.info("Send join request to server and retrieve token");
        var joinRequest = incentiveSystem.generateJoinRequest(providerPublicKey, userKeyPair, testPromotion.getPromotionParameters());
        var serializedJoinResponse = incentiveClient.sendJoinRequest(
                jsonConverter.serialize(joinRequest.getRepresentation()),
                jsonConverter.serialize(userKeyPair.getPk().getRepresentation()),
                testPromotion.getPromotionParameters().getPromotionId()
        ).block(Duration.ofSeconds(1));
        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), publicParameters);
        var token = incentiveSystem.handleJoinRequestResponse(testPromotion.getPromotionParameters(), providerPublicKey, userKeyPair, joinRequest, joinResponse);

        log.info("Create basket for testing credit-earn");
        var basketDto = TestHelper.createBasketWithItems(basketUrl);
        var items = basketClient.getItems().block(Duration.ofSeconds(1));

        var basket = new Basket(
                basketDto.getBasketID(),
                basketDto.getItems().entrySet().stream()
                        .map(stringIntegerEntry -> {
                            var basketItem = Arrays.stream(items).filter(item -> item.getId().equals(stringIntegerEntry.getKey())).findAny().get();
                            return new BasketItem(basketItem.getId(), basketItem.getTitle(), basketItem.getPrice(), stringIntegerEntry.getValue());
                        })
                        .collect(Collectors.toList())
        );

        /*
        log.info("Test earn with unpaid basket should fail");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                incentiveClient.sendBulkUpdates(basket.getBasketID(), new BulkRequestDto(List.of(new EarnRequestDto()), Collections.emptyList())).block())
                .withCauseInstanceOf(IncentiveClientException.class);
        */


        log.info("Run valid credit earn protocol");
        var earnRequest = incentiveSystem.generateEarnRequest(token, providerPublicKey, userKeyPair);
        var serializedEarnRequest = jsonConverter.serialize(earnRequest.getRepresentation());
        incentiveClient.sendBulkUpdates(basket.getBasketId(),
                new BulkRequestDto(
                        List.of(new EarnRequestDto(testPromotion.getPromotionParameters().getPromotionId(), serializedEarnRequest)),
                        Collections.emptyList()
                )
        ).block();

        basketClient.payBasket(basket.getBasketId(), basketDto.getValue(), paySecret).block();

        var serializedSignature = incentiveClient.retrieveBulkResults(basket.getBasketId()).block().getEarnTokenUpdateResultDtoList().get(0).getSerializedEarnResponse();
        var signature = new SPSEQSignature(
                jsonConverter.deserialize(serializedSignature),
                publicParameters.getBg().getG1(),
                publicParameters.getBg().getG2());
        var basketValueForPromotion = testPromotion.computeEarningsForBasket(basket);
        var newToken = incentiveSystem.handleEarnRequestResponse(
                testPromotion.getPromotionParameters(),
                earnRequest,
                signature,
                basketValueForPromotion,
                token,
                providerPublicKey,
                userKeyPair);
        Assertions.assertEquals(newToken.getPoints().map(RingElement::asInteger), basketValueForPromotion);

        /*
        log.info("Second earn with paid basket and same request should succeed");
        incentiveClient.sendEarnRequest(serializedEarnRequest, basket.getBasketID()).block();

        log.info("Test earn without valid basket should fail");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                incentiveClient.sendEarnRequest("Some request", UUID.randomUUID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);

        log.info("Second earn with paid basket and other request should fail");
        var otherEarnRequest = incentiveSystem.generateEarnRequest(token, providerPublicKey, userKeyPair);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                incentiveClient.sendEarnRequest(jsonConverter.serialize(otherEarnRequest.getRepresentation()), basket.getBasketID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);
         */
    }
}
