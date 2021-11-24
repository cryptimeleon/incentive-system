package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.client.InfoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DsprotectionApplication {
	@Value("${info-service.url}")
	private String infoServiceUrl = ""; // URL of info service

	public static void main(String[] args) {
		SpringApplication.run(DsprotectionApplication.class, args);
	}

	/**
	 * Initializes beans that are used for dependency injection throughout the entire package.
	 */

	@Bean
	InfoClient getInfoClient() {
		return new InfoClient(infoServiceUrl);
	}
}
