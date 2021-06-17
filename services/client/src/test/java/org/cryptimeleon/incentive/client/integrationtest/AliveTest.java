package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.CreditClient;
import org.cryptimeleon.incentive.client.IssueClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

public class AliveTest extends IncentiveSystemIntegrationTest {

    /*
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testCreditAlive() {
        var creditWebClient = WebClient.builder().baseUrl(creditUrl).build();
        var creditClient = new CreditClient(creditUrl);
        var result = creditClient.sendAliveRequest().block();
        assertThat(result).containsIgnoringCase("Credit");
        System.out.println("Result: " + result);
    }

    /*
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testBasketAlive() {
        var basketClient = new BasketClient(basketUrl);
        var result = basketClient.sendAliveRequest().block();
        assertThat(result).containsIgnoringCase("basket");
        System.out.println("Result: " + result);
    }

    /*
     * Integration test that queries the hello world endpoint
     */
    @Test
    void testIssueAlive() {
        var issueWebClient = WebClient.builder().baseUrl(issueUrl).build();
        var issueClient = new IssueClient(issueUrl);
        var result = issueClient.sendAliveRequest().block();
        assertThat(result).containsIgnoringCase("Issue");
        System.out.println("Result: " + result);
    }
}
