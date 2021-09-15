package org.cryptimeleon.incentive.services.deduct;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DeductApplication {

	// TODO: set default values for these endpoint URLs via environment variables as in CreditApplication
	private String infoServiceUrl; // URL of info service, needed to make requests to it when running the server side of Spend-Deduct

	private String basketServiceUrl; // URL of basket service, needed to make requests to it when running the server side of Spend-Deduct

	private String dsProtectionServiceUrl; // URL of dsprotection service, needed to make requests to it when running the server side of Spend-Deduct

	public static void main(String[] args) {
		SpringApplication.run(DeductApplication.class, args);
	}

	/**
	 * Initializes beans that are used for dependency injection throughout the entire package.
	 */

	@Bean
	InfoClient getInfoClient() {
		return new InfoClient(infoServiceUrl);
	}

	@Bean
	BasketClient getBasketClient() {
		return new BasketClient(basketServiceUrl);
	}

	@Bean
	DSProtectionClient getDsProtectionClient() {
		return new DSProtectionClient(dsProtectionServiceUrl);
	}
}