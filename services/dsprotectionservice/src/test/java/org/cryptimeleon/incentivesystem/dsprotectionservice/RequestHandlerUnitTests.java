package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;

import static org.mockito.Mockito.when;

/**
 * Unit tests for the handlers of the double-spending protection database management system.
 * Info service is mocked, meaning that its expected responses when querying public parameters are hard-coded.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestHandlerUnitTests {
    private Logger logger = LoggerFactory.getLogger(RequestHandlerUnitTests.class);

    @MockBean
    private CryptoRepository cryptoRepository;

    /**
     * Heartbeat check for double-spending protection service.
     */
    @Test
    public void aliveTest(@Autowired WebTestClient webClient)
    {
        logger.info("Heartbeat check for double-spending protection service.");
        String aliveResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/").build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        logger.info("Double-spending protection service is up and running: " + aliveResponse);
    }

    /**
     * Adds a transaction to the database. Verifies item count in database afterwards.
     */
    @Test
    public void addTransactionTest(@Autowired WebTestClient webClient) {
        // setup incentive system for the test (implicit, we only need public parameters)
        logger.info("Setting up (implicit) incentive system for the test.");
        IncentivePublicParameters pp = Setup.trustedSetup(512, Setup.BilinearGroupChoice.Debug);

        // JSON converter needed for serializing transactions
        logger.info("Generating JSON converter.");
        JSONConverter jsonConverter = new JSONConverter();

        logger.info("Setting up mocked repository for cryptographic assets.");
        when(cryptoRepository.getPp()).thenReturn(pp);

        logger.info("Generating and serializing valid random transaction and associated double-spending tag.");
        var ta1 = Helper.generateTransaction(pp, true);
        var serializedTa1Repr = Util.computeSerializedRepresentation(ta1);
        var serialiazedDsTag1Repr = Util.computeSerializedRepresentation(ta1.getDsTag());

        logger.info("Communicating with double-spending protection service to add transaction to DB.");
        String addTransactionResponse1 = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/addtransaction").build())
                .header("ta", serializedTa1Repr)
                .bodyValue(serialiazedDsTag1Repr)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        logger.info("Received response from endpoint: " + addTransactionResponse1);

        logger.info("Generating and serializing invalid random transaction and associated double-spending tag.");
        var ta2 = Helper.generateTransaction(pp, false);
        var serializedTa2Repr = Util.computeSerializedRepresentation(ta2);
        var serialiazedDsTag2Repr = Util.computeSerializedRepresentation(ta2.getDsTag());

        logger.info("Communicating with double-spending protection service to add transaction to DB.");
        String addTransactionResponse2 = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/addtransaction").build())
                .header("ta", serializedTa1Repr)
                .bodyValue(serialiazedDsTag1Repr)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        logger.info("Received response from endpoint: " + addTransactionResponse2);

        logger.info("Checking whether DB contains correct number of transactions");

        // retrieve all transactions to ensure that database contains exactly 2
        ArrayList<String> serializedTransactionList = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/retrieveallta").build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ArrayList.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertTrue(serializedTransactionList.size() == 2);
    }
}
