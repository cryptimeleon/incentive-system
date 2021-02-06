package de.upb.crypto.incentive.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreditClient {

    private WebClient creditClient;
    private String paySecret;
    private String redeemSecret;

    public Mono<String> sendHelloWorldRequest() {
        return creditClient.get()
                .uri("/test")
                .retrieve()
                .bodyToMono(String.class);
    }

}
