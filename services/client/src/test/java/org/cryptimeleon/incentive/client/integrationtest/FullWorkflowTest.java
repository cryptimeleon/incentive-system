package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.client.IssueClient;
import org.cryptimeleon.incentive.client.dto.JoinRequestDto;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * Test a full (correct) protocol flow.
 */
public class FullWorkflowTest extends IncentiveSystemIntegrationTest {
    @Test
    void runFullWorkflow() {
        // Setup clients
        var infoClient = new InfoClient(infoUrl);
        var issueClient = new IssueClient(issueUrl);

        // Retrieve data from issue service
        var serializedPublicParameters = infoClient.querySerializedPublicParameters().block(Duration.ofSeconds(1));
        var serializedProviderPublicKey = infoClient.querySerializedProviderPublicKey().block(Duration.ofSeconds(1));

        // Deserialize data and setup incentive system
        var jsonConverter = new JSONConverter();
        var publicParameters = new IncentivePublicParameters(jsonConverter.deserialize(serializedPublicParameters));
        var providerPublicKey = new ProviderPublicKey(jsonConverter.deserialize(serializedProviderPublicKey), publicParameters.getSpsEq(), publicParameters.getBg().getG1());
        var incentiveSystem = new IncentiveSystem(publicParameters);

        // Setup user data and generate join request
        var userKeyPair = incentiveSystem.generateUserKeys();
        var joinRequest= incentiveSystem.generateJoinRequest(publicParameters, providerPublicKey, userKeyPair);
        var joinRequestDTO = new JoinRequestDto(jsonConverter.serialize(userKeyPair.getPk().getRepresentation()), jsonConverter.serialize(joinRequest.getRepresentation()));

        // Send join request and handle result
        var serializedJoinResponse = issueClient.sendJoinRequest(joinRequestDTO).block(Duration.ofSeconds(1));
        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), publicParameters);
        var token = incentiveSystem.handleJoinRequestResponse(publicParameters, providerPublicKey, userKeyPair, joinRequest, joinResponse);
    }
}
