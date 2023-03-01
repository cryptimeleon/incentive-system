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

    @Value("${incentive.url}")
    String incentiveUrl;

    @Value("${basket.url}")
    String basketUrl;

    @Value("${dsprotection.url}")
    String dsProtectionUrl;

    @Value("${basket-service.redeem-secret}")
    String redeemSecret;

    @Value("${basket-service.provider-secret}")
    String basketProviderSecret;

    @Value("${provider.shared-secret}")
    String providerSharedSecret;

    @Value("${store.shared-secret}")
    String storeSharedSecret;

    @Value("${incentive-service.provider-secret}")
    String incentiveProviderSecret;
}
