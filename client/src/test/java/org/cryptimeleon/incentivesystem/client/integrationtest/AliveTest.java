package org.cryptimeleon.incentivesystem.client.integrationtest;

import org.cryptimeleon.incentivesystem.client.BasketserverClient;
import org.cryptimeleon.incentivesystem.client.CreditClient;
import org.cryptimeleon.incentivesystem.client.IssueClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

public class AliveTest extends IncentiveSystemIntegrationTest {

    /*
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testCreditAlive() {
        var creditClient = new CreditClient(creditUrl);
        var result = creditClient.sendAliveRequest().block();
        assertThat(result).containsIgnoringCase("Credit");
        System.out.println("Result: " + result);
    }

    /*
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testBasketserverAlive() {
        var basketserverClient = new BasketserverClient(basketserverUrl);
        var result = basketserverClient.sendAliveRequest().block();
        assertThat(result).containsIgnoringCase("Basketserver");
        System.out.println("Result: " + result);
    }

    /*
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testIssueAlive() {
        var issueClient = new IssueClient(issueUrl);
        var result = issueClient.sendAliveRequest().block();
        assertThat(result).containsIgnoringCase("Issue");
        System.out.println("Result: " + result);
    }
}
