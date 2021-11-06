package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
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
        var ta1 = Helper.generateTransaction(cryptoRepository.getPp(), true);

        logger.info("Communicating with double-spending protection service to add transaction to DB.");
        String addTransactionResponse1 = this.addTransactionNode(ta1, webClient);
        logger.info("Received response from endpoint: " + addTransactionResponse1);

        logger.info("Generating and serializing invalid random transaction and associated double-spending tag.");
        var ta2 = Helper.generateTransaction(cryptoRepository.getPp(), false);

        logger.info("Communicating with double-spending protection service to add transaction to DB.");
        String addTransactionResponse2 = this.addTransactionNode(ta2, webClient);
        logger.info("Received response from endpoint: " + addTransactionResponse2);

        logger.info("Checking whether DB contains correct number of transactions.");

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

        logger.info("Checking whether DB contains both individual transactions.");

        boolean containsTa1 = this.containsTransaction(ta1.getTaIdentifier(), webClient);
        Assertions.assertTrue(containsTa1);

        boolean containsTa2 = this.containsTransaction(ta2.getTaIdentifier(), webClient);
        Assertions.assertTrue(containsTa2);

        logger.info("Retrieving second stored transaction from database.");

        var serializedTa2Id = Util.computeSerializedRepresentation(
                new TransactionIdentifier(
                        ta2.getTransactionID(),
                        ta2.getDsTag().getGamma()
                )
        );

        // GET-request to double-spending protection service to obtain transaction from DB
        String retrievedSerializedTa2Repr = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/gettransaction").build())
                .header("taid", serializedTa2Id)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        Transaction retrievedTa2 = new Transaction(
                new JSONConverter().deserialize(retrievedSerializedTa2Repr),
                cryptoRepository.getPp()
        );

        logger.info("Checking whether original second transaction was retrieved correctly");

        Assertions.assertTrue(ta2.equals(retrievedTa2));
    }

    /**
     * Adds a token to the database. Verifies token count in the database afterwards.
     */
    @Test
    public void addTokenTest(@Autowired WebTestClient webClient)
    {

    }


    /**
     * helper methods
     */

    /**
     * Adds the passed transaction to the database.
     */
    public String addTransactionNode(Transaction ta, WebTestClient webClient) {
        var serializedTaRepr = Util.computeSerializedRepresentation(ta);
        var serialiazedDsTagRepr = Util.computeSerializedRepresentation(ta.getDsTag());

        // POST request to double-spending protection service
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/addtransaction").build())
                .header("ta", serializedTaRepr)
                .bodyValue(serialiazedDsTagRepr)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
    }

    public boolean containsTransaction(TransactionIdentifier taId, WebTestClient webClient) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/containsta").build())
                .header("taidgamma", Util.computeSerializedRepresentation(taId))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(boolean.class)
                .returnResult()
                .getResponseBody();
    }
}
