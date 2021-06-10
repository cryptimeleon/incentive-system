package org.cryptimeleon.incentivesystem.services.issue;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.web.util.UriBuilder;

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