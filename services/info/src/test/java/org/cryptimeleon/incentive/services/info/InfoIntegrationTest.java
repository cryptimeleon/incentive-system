package org.cryptimeleon.incentive.services.info;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;


/**
 * Integration test that tests correct behavior of this service.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class InfoIntegrationTest {

    @Value("${provider.shared-secret}")
    String sharedSecret;

    JSONConverter jsonConverter = new JSONConverter();

    @Autowired
    private InfoService infoService;

    /**
     * Test public parameter endpoint.
     */
    @Test
    void requestPublicParameters(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/public-parameters")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    JSONConverter jsonConverter = new JSONConverter();
                    IncentivePublicParameters incentivePublicParameters = new IncentivePublicParameters(jsonConverter.deserialize(response.getResponseBody()));
                });
    }

    /**
     * Test provider public key endpoint.
     */
    @Test
    void requestProviderPublicKey(@Autowired WebTestClient webClient) {
        String serializedPublicParameters = infoService.getSerializedPublicParameters();
        IncentivePublicParameters publicParameters = new IncentivePublicParameters(jsonConverter.deserialize(serializedPublicParameters));

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/provider-public-key")
                        .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    new ProviderPublicKey(jsonConverter.deserialize(response.getResponseBody()), publicParameters);
                });
    }

    /**
     * Test provider secret key endpoint.
     * An unauthenticated request should result in a 4xx error, and authenticated request should be successful.
     */
    @Test
    void requestProviderSecretKey(@Autowired WebTestClient webClient) {
        String serializedPublicParameters = infoService.getSerializedPublicParameters();
        IncentivePublicParameters publicParameters = new IncentivePublicParameters(jsonConverter.deserialize(serializedPublicParameters));

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/provider-secret-key")
                        .build())
                .exchange()
                .expectStatus()
                .is4xxClientError();

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/provider-secret-key")
                        .build())
                .header("shared-secret", sharedSecret)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    new ProviderSecretKey(jsonConverter.deserialize(response.getResponseBody()), publicParameters);
                });
    }
}
