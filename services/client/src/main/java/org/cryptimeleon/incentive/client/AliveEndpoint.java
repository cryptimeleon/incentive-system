package org.cryptimeleon.incentive.client;

import reactor.core.publisher.Mono;

public interface AliveEndpoint {
    Mono<String> sendAliveRequest();
}
