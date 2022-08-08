package org.cryptimeleon.incentive.client.integrationtest;


import org.junit.jupiter.api.Test;

/**
 * Failing test to check pipeline works as expected!
 */
public class FailingTest extends IncentiveSystemIntegrationTest {

    @Test
    void failingTest() {
        throw new RuntimeException("Test Failed!");
    }
}
