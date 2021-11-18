package org.cryptimeleon.incentive.services.credit;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.BasketDto;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigInteger;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Integration test that tests correct behavior of this service.
 * The info service and basket service are mocked.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CreditIntegrationTest {

    @MockBean
    private CryptoRepository cryptoRepository;

    @MockBean
    private BasketRepository basketRepository;

    @Test
    void validRequestTest(@Autowired WebTestClient webClient) {
        // Setup the incentive system for the test
        IncentivePublicParameters pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
        ProviderKeyPair pkp = Setup.providerKeyGen(pp);
        UserKeyPair ukp = Setup.userKeyGen(pp);
        PromotionParameters promotionParameters = incentiveSystem.legacyPromotionParameters();
        JSONConverter jsonConverter = new JSONConverter();

        // Setup the mock of the crypto repository
        when(cryptoRepository.getPublicParameters()).thenReturn(pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(pkp.getPk());
        when(cryptoRepository.getProviderSecretKey()).thenReturn(pkp.getSk());

        // Setup basket repository mock
        var earnAmount = 42;
        var testBasket = new BasketDto();
        var items = Collections.singletonMap("1234123412", earnAmount);
        testBasket.setBasketID(UUID.randomUUID());
        testBasket.setPaid(true);
        testBasket.setValue(earnAmount);
        testBasket.setItems(items);
        when(basketRepository.getBasket(testBasket.getBasketID())).thenReturn(testBasket);

        // Generate token
        var joinRequest = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp);
        var joinResponse = incentiveSystem.generateJoinRequestResponse(promotionParameters, pkp, ukp.getPk().getUpk(), joinRequest);
        var token = incentiveSystem.handleJoinRequestResponse(promotionParameters, pkp.getPk(), ukp, joinRequest, joinResponse);

        // Generate request
        var earnRequest = incentiveSystem.generateEarnRequest(token, pkp.getPk(), ukp);

        // Send request and verify result
        var serializedSignature = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/credit").build())
                .header("basket-id", testBasket.getBasketID().toString())
                .header("earn-request", jsonConverter.serialize(earnRequest.getRepresentation()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        var signature = new SPSEQSignature(jsonConverter.deserialize(serializedSignature), pp.getBg().getG1(), pp.getBg().getG2());
        var newToken = incentiveSystem.handleEarnRequestResponse(promotionParameters, earnRequest, signature, Vector.of(BigInteger.valueOf(earnAmount)), token, pkp.getPk(), ukp);

        Assertions.assertEquals(newToken.getPoints().get(0).asInteger(), token.getPoints().get(0).asInteger().add(BigInteger.valueOf(earnAmount)));
    }

    /**
     * This test ensures that users cannot use unpaid baskets to earn points.
     */
    @Test
    void unpaidBasketTest(@Autowired WebTestClient webClient) {
        // Construct unpaid basket and add to mock
        var earnAmount = 42;
        var testBasket = new BasketDto();
        var items = Collections.singletonMap("1234123412", earnAmount);
        testBasket.setBasketID(UUID.randomUUID());
        testBasket.setPaid(false);  // Unpaid baskets cannot be redeemed
        testBasket.setValue(earnAmount);
        testBasket.setItems(items);
        when(basketRepository.getBasket(testBasket.getBasketID())).thenReturn(testBasket);

        // Ensure that request is blocked
        webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/credit").build())
                .header("basket-id", testBasket.getBasketID().toString())
                .header("earn-request", "some-request")
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}