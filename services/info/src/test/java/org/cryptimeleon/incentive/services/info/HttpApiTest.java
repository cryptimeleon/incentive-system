package org.cryptimeleon.incentivesystem.services.info;

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


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpApiTest {

    @Value("${provider.shared-secret}")
    String sharedSecret;

    JSONConverter jsonConverter = new JSONConverter();

    @Autowired
    private InfoService infoService;

    /*
     * Assert that public parameters are sent and can be deserialized
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
                    ProviderPublicKey providerPublicKey = new ProviderPublicKey(jsonConverter.deserialize(response.getResponseBody()), publicParameters.getSpsEq(), publicParameters.getBg().getG1());
                });
    }

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
                    ProviderSecretKey providerSecretKey = new ProviderSecretKey(jsonConverter.deserialize(response.getResponseBody()), publicParameters.getSpsEq(), publicParameters.getBg().getZn(), publicParameters.getPrfToZn());
                });
    }
}
