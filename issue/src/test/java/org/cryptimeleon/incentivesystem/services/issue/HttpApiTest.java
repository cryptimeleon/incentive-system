package org.cryptimeleon.incentivesystem.services.issue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

import java.util.UUID;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpApiTest {

    private java.net.URI buildRequestUri(UriBuilder uriBuilder, String serializedJoinRequest, String serializedUserPublicKey) {
        return uriBuilder
                .path("/issue")
                .queryParam("serializedUserPublicKey", serializedUserPublicKey)
                .queryParam("serializedJoinRequest", serializedJoinRequest)
                .build();
    }

    // TODO write test, how to inject incentive-system?
}