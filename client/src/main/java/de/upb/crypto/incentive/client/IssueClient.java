package de.upb.crypto.incentive.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class IssueClient {

    private WebClient issueClient;

    public Mono<String> sendAliveRequest() {
        return issueClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

}
