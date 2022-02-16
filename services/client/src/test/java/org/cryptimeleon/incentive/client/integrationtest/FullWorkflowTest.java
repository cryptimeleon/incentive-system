package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.RewardSideEffect;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Test a full (correct) protocol flow.
 */
@Slf4j
public class FullWorkflowTest extends IncentiveSystemIntegrationTest {

    Promotion testPromotion = new HazelPromotion(HazelPromotion.generatePromotionParameters(),
            "Test Promotion",
            "Some Test Promotion",
            List.of(new HazelTokenUpdate(UUID.randomUUID(), "This is a test reward", new RewardSideEffect("Test Reward Sideffect"), 2)),
            "Apple");

    @Test
    void runFullWorkflow() {
        var infoClient = new InfoClient(infoUrl);
        var incentiveClient = new IncentiveClient(incentiveUrl);
        var basketClient = new BasketClient(basketUrl);

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
        incentiveClient.addPromotions(List.of(testPromotion)).block(Duration.ofSeconds(1)).getStatusCode().is2xxSuccessful();

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
        var basket = TestHelper.createBasketWithItems(basketUrl);

        /*
         * These test cases will be rewritten alongside their implementation in the app
        log.info("Test earn with unpaid basket should fail");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                incentiveClient.sendEarnRequest("Some request", basket.getBasketID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);
        basketClient.payBasket(basket.getBasketID(), basket.getValue(), paySecret).block();

        log.info("Run valid credit earn protocol");
        var earnRequest = incentiveSystem.generateEarnRequest(token, providerPublicKey, userKeyPair);
        var serializedEarnRequest = jsonConverter.serialize(earnRequest.getRepresentation());
        var serializedSignature = incentiveClient.sendEarnRequest(serializedEarnRequest, basket.getBasketID()).block();
        var signature = new SPSEQSignature(jsonConverter.deserialize(serializedSignature), publicParameters.getBg().getG1(), publicParameters.getBg().getG2());
        var newToken = incentiveSystem.handleEarnRequestResponse(
                testPromotion.getPromotionParameters(),
                earnRequest,
                signature,
                Vector.of(BigInteger.valueOf(basket.getValue())),
                token,
                providerPublicKey,
                userKeyPair);
        Assertions.assertEquals(newToken.getPoints().get(0).asInteger().longValueExact(), basket.getValue());

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
