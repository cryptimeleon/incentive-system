package org.cryptimeleon.incentive.client.integrationtest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;


/*
 * Base for all integration tests.
 * Uses the development configuration, loads url and shared secrets needed for testing.
 */
@SpringBootTest(classes = TestApplicationConfiguration.class)
public abstract class IncentiveSystemIntegrationTest {

    @Value("${info.url}")
    String infoUrl;

    @Value("${issue.url}")
    String issueUrl;

    @Value("${credit.url}")
    String creditUrl;

    @Value("${basket.url}")
    String basketUrl;

    @Value("${basket-service.redeem-secret}")
    String redeemSecret;

    @Value("${basket-service.pay-secret}")
    String paySecret;

}
