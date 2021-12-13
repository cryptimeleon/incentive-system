package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.*;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Test a full (correct) protocol flow.
 */
@Slf4j
public class FullWorkflowTest extends IncentiveSystemIntegrationTest {


    @Test
    void runFullWorkflow() {
        var infoClient = new InfoClient(infoUrl);
        var issueClient = new IssueClient(issueUrl);
        var creditClient = new CreditClient(creditUrl);
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
        var promotionParameters = incentiveSystem.legacyPromotionParameters();
        var userKeyPair = incentiveSystem.generateUserKeys();

        log.info("Send join request to server and retrieve token");
        var joinRequest = incentiveSystem.generateJoinRequest(providerPublicKey, userKeyPair);
        var serializedJoinResponse = issueClient.sendJoinRequest(
                jsonConverter.serialize(joinRequest.getRepresentation()),
                jsonConverter.serialize(userKeyPair.getPk().getRepresentation())
        ).block(Duration.ofSeconds(1));
        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), publicParameters);
        var token = incentiveSystem.handleJoinRequestResponse(promotionParameters, providerPublicKey, userKeyPair, joinRequest, joinResponse);

        log.info("Create basket for testing credit-earn");
        var basket = TestHelper.createBasketWithItems(basketUrl);

        log.info("Test earn with unpaid basket should fail");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                creditClient.sendEarnRequest("Some request", basket.getBasketID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);
        basketClient.payBasket(basket.getBasketID(), basket.getValue(), paySecret).block();

        log.info("Run valid credit earn protocol");
        var earnRequest = incentiveSystem.generateEarnRequest(token, providerPublicKey, userKeyPair);
        var serializedEarnRequest = jsonConverter.serialize(earnRequest.getRepresentation());
        var serializedSignature = creditClient.sendEarnRequest(serializedEarnRequest, basket.getBasketID()).block();
        var signature = new SPSEQSignature(jsonConverter.deserialize(serializedSignature), publicParameters.getBg().getG1(), publicParameters.getBg().getG2());
        var newToken = incentiveSystem.handleEarnRequestResponse(
                promotionParameters,
                earnRequest,
                signature,
                Vector.of(BigInteger.valueOf(basket.getValue())),
                token,
                providerPublicKey,
                userKeyPair);
        Assertions.assertEquals(newToken.getPoints().get(0).asInteger().longValueExact(), basket.getValue());

        log.info("Second earn with paid basket and same request should succeed");
        creditClient.sendEarnRequest(serializedEarnRequest, basket.getBasketID()).block();

        log.info("Test earn without valid basket should fail");
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                creditClient.sendEarnRequest("Some request", UUID.randomUUID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);

        log.info("Second earn with paid basket and other request should fail");
        var otherEarnRequest = incentiveSystem.generateEarnRequest(token, providerPublicKey, userKeyPair);
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() ->
                creditClient.sendEarnRequest(jsonConverter.serialize(otherEarnRequest.getRepresentation()), basket.getBasketID()).block())
                .withCauseInstanceOf(IncentiveClientException.class);
    }
}
