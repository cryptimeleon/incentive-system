package org.cryptimeleon.incentivesystem.dsprotectionservice;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
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
        logger.info("Started transaction adding test.");

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
                ta2.getTaIdentifier()
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
     * Adds some random token nodes to the database and links random user info to them.
     */
    @Test
    public void addTokenTest(@Autowired WebTestClient webClient)
    {
        logger.info("Started token adding test.");

        // setup incentive system for the test (implicit, we only need public parameters)
        logger.info("Setting up (implicit) incentive system for the test.");
        IncentivePublicParameters pp = Setup.trustedSetup(512, Setup.BilinearGroupChoice.Debug);

        // JSON converter needed for serializing transactions
        logger.info("Generating JSON converter.");
        JSONConverter jsonConverter = new JSONConverter();

        logger.info("Setting up mocked repository for cryptographic assets.");
        when(cryptoRepository.getPp()).thenReturn(pp);

        logger.info("Generate two random double-spending ids to add to the database.");
        GroupElement dsid1 = cryptoRepository.getPp().getBg().getG1().getUniformlyRandomElement();
        GroupElement dsid2 = cryptoRepository.getPp().getBg().getG1().getUniformlyRandomElement();

        logger.info("Communicating with double-spending protection service to add double-spending IDs to the database.");
        String addTokenResponse1 = this.addTokenNode(dsid1, webClient);
        logger.info("Received response from endpoint: " + addTokenResponse1);
        String addTokenResponse2 = this.addTokenNode(dsid2, webClient);
        logger.info("Received response from endpoint: " + addTokenResponse2);

        logger.info("Checking database for containment of the two double-spending IDs.");
        boolean containsDsid1 = this.containsTokenNode(dsid1, webClient);
        Assertions.assertTrue(containsDsid1);
        boolean containsDsid2 = this.containsTokenNode(dsid2, webClient);
        Assertions.assertTrue(containsDsid2);

        logger.info("Generating random user info.");
        UserInfo uInfo1 = Helper.generateUserInfo(cryptoRepository.getPp());
        UserInfo uInfo2 = Helper.generateUserInfo(cryptoRepository.getPp());

        logger.info("Adding user info to database and linking it to double-spending IDs");
        String addUserInfo1Response = this.addAndLinkUserInfo(uInfo1, dsid1, webClient);
        logger.info("Received response from endpoint: " + addUserInfo1Response);
        String addUserInfo2Response = this.addAndLinkUserInfo(uInfo2, dsid2, webClient);
        logger.info("Received response from endpoint: " + addUserInfo2Response);

        logger.info("Checking whether user info was correctly associated to token nodes.");
        UserInfo retrievedUserInfo1 = new UserInfo(
                jsonConverter.deserialize(this.getUserInfo(dsid1, webClient)),
                cryptoRepository.getPp()
        );
        Assertions.assertTrue(retrievedUserInfo1.equals(uInfo1)); // TODO: fails, why?
        UserInfo retrievedUserInfo2 = new UserInfo(
                jsonConverter.deserialize(this.getUserInfo(dsid2, webClient)),
                cryptoRepository.getPp()
        );
        Assertions.assertTrue(retrievedUserInfo2.equals(uInfo2));
    }

    /**
     * Adds some transactions and tokens to the database, then connects them via edges and
     * checks whether edges are contained and
     * whether consumed tokens/consuming transactions are returned correctly when queried.
     */
    @Test
    public void edgeTest() {
        // TODO: implement
    }

    /**
     * Adds a valid transaction and invalidates it twice.
     * Checks whether transaction is still invalid in the end.
     */
    @Test
    public void invalidateTaTest() {
        // TODO: implement
    }







    /**
     * helper methods
     */






    /**
     * Adds the passed transaction to the database.
     * @param webClient client for connection to double-spending protection service
     * @return server response body content
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

    /**
     * Checks database for containment of a transaction node with the passed identifier.
     * @param webClient client for connection to double-spending protection service
     * @return boolean
     */
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

    /**
     * Adds the passed dsid to the database as a token node.
     * @param webClient client for connection to double-spending protection service
     * @return server response body content
     */
    public String addTokenNode(GroupElement dsid, WebTestClient webClient) {
        var serializedDsidRepr = Util.computeSerializedRepresentation(dsid);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/adddsid").build())
                .header("dsid", serializedDsidRepr)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
    }

    /**
     * Checks the database for containment of a token node with the passed dsid.
     * @param webClient client for connection to double-spending protection service
     * @return boolean
     */
    public boolean containsTokenNode(GroupElement dsid, WebTestClient webClient) {
        var serializedDsidRepr = Util.computeSerializedRepresentation(dsid);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/containsdsid").build())
                .header("dsid", serializedDsidRepr)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(boolean.class)
                .returnResult()
                .getResponseBody();
    }

    /**
     * Adds passed user info to the database and links it to the passed dsid.
     * @param webClient client for connection to double-spending protection service
     * @return server response body content
     */
    public String addAndLinkUserInfo(UserInfo uInfo, GroupElement dsid, WebTestClient webClient) {
        var serializedUserInfoRepr = Util.computeSerializedRepresentation(uInfo);
        var serializedDsidRepr = Util.computeSerializedRepresentation(dsid);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/adduserinfo").build())
                .header("dsid", serializedDsidRepr)
                .bodyValue(serializedUserInfoRepr)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
    }

    /**
     * Retrieves the user info associated to the token node with the passed double-spending ID.
     * @param webClient client for connection to double-spending protection service
     * @return serialized representation of user info
     */
    public String getUserInfo(GroupElement dsid, WebTestClient webClient) {
        var serializedDsidRepr = Util.computeSerializedRepresentation(dsid);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/getuserinfo").build())
                .header("dsid", serializedDsidRepr)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
    }
}
