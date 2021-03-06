package org.cryptimeleon.incentivesystem.client.integrationtest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;


/*
 * Base for all integration tests.
 * Uses the development configuration, loads url and shared secrets needed for testing.
 */
@SpringBootTest(classes = TestApplicationConfiguration.class)
public abstract class IncentiveSystemIntegrationTest {

    @Value("${issue.url}")
    String issueUrl;

    @Value("${credit.url}")
    String creditUrl;

    @Value("${basketserver.url}")
    String basketserverUrl;

    @Value("${basketserver.redeem-secret}")
    String redeemSecret;

    @Value("${basketserver.pay-secret}")
    String paySecret;

}
