package org.cryptimeleon.incentive.services.issue;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.BDDMockito.when;

/**
 * Integration test of this service.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class IssueIntegrationTest {

    /**
     * Use a MockBean to prevent the CryptoRepository from being created (and trying to connect to the info service)
     */
    @MockBean
    CryptoRepository cryptoRepository;

    /**
     * Test the issue-join protocol using a http client.
     */
    @Test
    public void issueJoinTest(@Autowired WebTestClient webClient) {
        // Setup the incentive system for the test
        IncentivePublicParameters pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
        ProviderKeyPair pkp = Setup.providerKeyGen(pp);
        UserKeyPair ukp = Setup.userKeyGen(pp);
        JSONConverter jsonConverter = new JSONConverter();

        // Setup the mock to return the correct values
        when(cryptoRepository.getPublicParameters()).thenReturn(pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(pkp.getPk());
        when(cryptoRepository.getProviderSecretKey()).thenReturn(pkp.getSk());

        // Create request to send
        var joinRequest = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp);

        // Send request and process response to assert correct behavior
        var serializedJoinResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/issue")
                        .build())
                .header("public-key", jsonConverter.serialize(ukp.getPk().getRepresentation()))
                .header("join-request", jsonConverter.serialize(joinRequest.getRepresentation()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), pp);
        incentiveSystem.handleJoinRequestResponse(pkp.getPk(), ukp, joinRequest, joinResponse);
    }
}