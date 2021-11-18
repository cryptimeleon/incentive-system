package org.cryptimeleon.incentive.services.promotion;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.promotion.promotions.NutellaPromotion;
import org.cryptimeleon.incentive.promotion.promotions.Promotion;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PromotionServiceTest {

    /**
     * Use a MockBean to prevent the CryptoRepository from being created (and trying to connect to the info service)
     */
    @MockBean
    CryptoRepository cryptoRepository;

    @Test
    public void promotionServiceTest(@Autowired WebTestClient webClient) {
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

        String[] serializedPromotions = webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/promotions").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String[].class)
                .returnResult().getResponseBody();

        assert serializedPromotions != null;
        List<Promotion> promotions = Arrays.stream(serializedPromotions)
                .map(s -> new NutellaPromotion(jsonConverter.deserialize(s))).collect(Collectors.toList());

        Promotion promotionToJoin = promotions.get(0);

        // Create request to send
        var joinRequest = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp);

        // Send request and process response to assert correct behavior
        var serializedJoinResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/join-promotion")
                        .build())
                .header("user-public-key", jsonConverter.serialize(ukp.getPk().getRepresentation()))
                .header("join-request", jsonConverter.serialize(joinRequest.getRepresentation()))
                .header("promotion-id", String.valueOf(promotionToJoin.promotionParameters.getPromotionId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), pp);
        incentiveSystem.handleJoinRequestResponse(promotionToJoin.promotionParameters, pkp.getPk(), ukp, joinRequest, joinResponse);

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
    }
}
