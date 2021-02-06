package de.upb.crypto.incentive.basketserver.client;

import de.upb.crypto.incentive.basketserver.model.Basket;
import de.upb.crypto.incentive.basketserver.model.requests.RedeemBasketRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BasketServerClient {

	private WebClient client;
	private String paySecret;
	private String redeemSecret;

	public Mono<Basket> getBasket(UUID basketId) {
		return client.get()
				.uri("/basket")
				.header("basketId", String.valueOf(basketId))
				.retrieve()
				.bodyToMono(Basket.class);
	}

	public Mono<Void> redeem(RedeemBasketRequest redeemBasketRequest) {
		return  client.post()
				.uri("/basket/redeem")
                .header("redeem-secret", redeemSecret)
				.body(BodyInserters.fromValue(redeemBasketRequest))
				.retrieve()
				.bodyToMono(Void.class);
	}
}
