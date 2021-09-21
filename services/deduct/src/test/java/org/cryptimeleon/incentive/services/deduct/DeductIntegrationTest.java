package org.cryptimeleon.incentive.services.deduct;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigInteger;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Integration test for the Deduct web service.
 * Basket and info service are mocked (means that their expected responses to requests are hard-coded).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeductIntegrationTest {
    @MockBean
    private CryptoRepository cryptoRepository;

    @MockBean
    private BasketRepository basketRepository;

    private Logger logger = LoggerFactory.getLogger(DeductIntegrationTest.class);

    /**
     * Lets the Deduct service handle a valid spend request and verifies that correct amount of point is removed from token.
     */
    @Test
    public void validSpendOperationTest(@Autowired WebTestClient webClient) {
        logger.info("Starting test for valid spend operation.");
        BigInteger points = new BigInteger("445");
        /**
         * Due to the missing basket server endpoint for querying the trusted spend amount (resp. trusted transaction ID), the spend amount (resp. transaction ID) is currently hard-coded in DeductService.runDeduct.
         * So until we implement the said endpoint, we cannot use other spend amounts than 231 (resp. other tids than one element).
         * TODO: implement said basket server endpoint
         */
        BigInteger spendAmount = new BigInteger("231");
        spendOperationTest(points, spendAmount, webClient);
    }

    /**
     * Sends a spend request to the Deduct service that attempts to spend more points than the token contains.
     */
    @Test
    public void notEnoughPointsTest(@Autowired WebTestClient webClient) {
        logger.info("Starting test for invalid spend operation (not enough points).");
        BigInteger points = new BigInteger("42");
        BigInteger spendAmount = new BigInteger("231");
        spendOperationTest(points, spendAmount, webClient);
    }

    /**
     * Performs a spend operation spending a certain amount of points from a token that contains the passed number of points.
     * @param points amount of points that the token contains before the spend operation
     * @param spendAmount amount of points to be spent
     * @param webClient client object making the request to the Deduct service
     */
    public void spendOperationTest(BigInteger points, BigInteger spendAmount, WebTestClient webClient) {
        // setup the incentive system for the test
        logger.info("Setting up incentive system for the test.");
        IncentivePublicParameters pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug); // generate public parameters
        IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
        ProviderKeyPair pkp = Setup.providerKeyGen(pp);

        // create a JSON converter for (de-)serialization
        logger.info("Creating JSON converter.");
        JSONConverter jsonConverter = new JSONConverter();

        // setup mocked crypto repository
        logger.info("Setting up mocked repository for crypto assets.");
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem); // the system instance
        when(cryptoRepository.getPp()).thenReturn(pp); // public parameters
        when(cryptoRepository.getPk()).thenReturn(pkp.getPk()); // provider public key
        when(cryptoRepository.getSk()).thenReturn(pkp.getSk()); // provider secret key

        // setup mocked basket repository
        // TODO: can only be done once BasketRepository is implemented (which can only be done after addressing endpoint issue for basket server)
        // TODO: for now, spend amount is passed in the method parameters. We need to rewrite this test template once we implemented the basket server end point for querying the spend amount (create a mock basket in the mock basket server, query the spend amount for it)

        // generate fresh user key pair
        logger.info("Generating user key pair.");
        UserKeyPair ukp = Setup.userKeyGen(pp);

        // generate token by simulating Issue-Join
        logger.info("Generating token with " + points.toString() + " points.");
        var joinRequest = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp);
        var joinResponse = incentiveSystem.generateJoinRequestResponse(pkp, ukp.getPk().getUpk(), joinRequest);
        var token = incentiveSystem.handleJoinRequestResponse(pkp.getPk(), ukp, joinRequest, joinResponse);

        // add points to token by simulating Credit-Earn
        var earnRequest = incentiveSystem.generateEarnRequest(token, pkp.getPk(), ukp);
        var signatureResponse = incentiveSystem.generateEarnRequestResponse(earnRequest, points, pkp);
        token = incentiveSystem.handleEarnRequestResponse(earnRequest, signatureResponse, points, token, pkp.getPk(), ukp);

        try{
            // generate and serialize spend request
            logger.info("Preparing to spend " + spendAmount.toString() + " points.");
            Zn.ZnElement transactionID = pp.getBg().getZn().getOneElement(); // TODO: remove this hard-coded tid once basket service endpoint works, other transaction IDs than oneElement won't work until then since tid is hard-coded in DeductService.runDeduct
            UUID basketID = UUID.randomUUID();
            var spendRequest = incentiveSystem.generateSpendRequest(token, pkp.getPk(), spendAmount, ukp, transactionID);
            var serializedSpendRequest = jsonConverter.serialize(spendRequest.getRepresentation());

            // send a request to the Deduct service to spend some points, receive and deserialize spend response
            logger.info("Communicating with Deduct service.");
            String serializedSpendResponse = null;

            // HTTP request marshalling
            serializedSpendResponse = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/deduct").build())
                    .header("basket-id", basketID.toString())
                    .bodyValue(serializedSpendRequest) // spend request must be sent in the body since too large for header
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();
            logger.info("Successfully received spend response.");

            // deserializing answer
            logger.info("Deserializing...");
            var spendResponseRepresentation = jsonConverter.deserialize(serializedSpendResponse);
            var spendResponse = new SpendResponse(spendResponseRepresentation, pp.getBg().getZn(), pp.getSpsEq());
            logger.info("Done");

            // handle spend response
            logger.info("Updating token.");
            token = incentiveSystem.handleSpendRequestResponse(spendResponse, spendRequest, token, spendAmount, pkp.getPk(), ukp);

            // check for outcome correctness
            logger.info("Verifying token point count.");
            Assertions.assertEquals(token.getPoints().getInteger(), points.subtract(spendAmount));
        }
        // cover case that token does not contain enough points
        catch(IllegalArgumentException e) {
            logger.info(e.getMessage());
        }
    }
}