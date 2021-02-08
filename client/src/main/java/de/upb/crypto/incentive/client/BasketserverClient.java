package de.upb.crypto.incentive.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class BasketserverClient {

    private WebClient basketClient;

    public Mono<String> sendAliveRequest() {
        return basketClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

}
