package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.CreditClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.client.IssueClient;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class AliveTest extends IncentiveSystemIntegrationTest {

    /**
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testCreditAlive() {
        var creditClient = new CreditClient(creditUrl);
        var result = creditClient.sendAliveRequest().block(Duration.ofSeconds(5));
        assertThat(result).containsIgnoringCase("Credit");
        System.out.println("Result: " + result);
    }

    /**
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testBasketAlive() {
        var basketClient = new BasketClient(basketUrl);
        var result = basketClient.sendAliveRequest().block(Duration.ofSeconds(5));
        assertThat(result).containsIgnoringCase("basket");
        System.out.println("Result: " + result);
    }

    /**
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testInfoAlive() {
        var infoClient = new InfoClient(infoUrl);
        var result = infoClient.sendAliveRequest().block(Duration.ofSeconds(5));
        assertThat(result).containsIgnoringCase("Info");
        System.out.println("Result: " + result);
    }

    /**
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testIssueAlive() {
        var issueClient = new IssueClient(issueUrl);
        var result = issueClient.sendAliveRequest().block(Duration.ofSeconds(5));
        assertThat(result).containsIgnoringCase("Issue");
        System.out.println("Result: " + result);
    }
}
