package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.SpendRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResultsDto;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.TestSuiteWithPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.incentive.services.incentive.api.RegistrationCouponJSON;
import org.cryptimeleon.incentive.services.incentive.repository.BasketRepository;
import org.cryptimeleon.incentive.services.incentive.repository.CryptoRepository;
import org.cryptimeleon.incentive.services.incentive.repository.OfflineDSPRepository;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigInteger;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.cryptimeleon.incentive.services.incentive.ClientHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests all the functionality of the incentive service.
 * This includes the server side of the crypto protocols (Issue-Join, Credit-Earn, Spend-Deduct)
 * and the issuing of registration tokens.
 * <p>
 * Uses a WebTestClient object as the client for the crypto protocol tests.
 * The servers that the incentive service communicates with
 * (namely basket, info and double-spending protection service)
 * are mocked using hard-coded answers to the test queries.
 * <p>
 * The incentive system instance used for all tests is the hard-coded one from the crypto.testFixtures package.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IncentiveServiceTest {
    // public parameters, incentive system and key pairs from crypto.testFixtures
    private static final IncentivePublicParameters pp = TestSuite.pp;
    private static final IncentiveSystem incentiveSystem = TestSuite.incentiveSystem;
    private static final ProviderKeyPair pkp = TestSuite.providerKeyPair;
    private static final UserKeyPair ukp = TestSuite.userKeyPair;

    // JSON converter for marshalling/de-marshalling of query data
    private static final JSONConverter jsonConverter = new JSONConverter();

    // hard-coded baskets that the mock basket repo is set up with
    private final Basket testBasket = new Basket(
            UUID.randomUUID(),
            List.of(
                    new BasketItem(UUID.randomUUID().toString(), "Hazelnut Spread", 200, 5),
                    new BasketItem(UUID.randomUUID().toString(), "Large Hazelnut Spread", 100, 3)
            )
    );
    private final Basket emptyTestBasket = new Basket(
            UUID.randomUUID(),
            List.of()
    );
    // For DSP testing
    private final Basket emptyTestBasketTwo = new Basket(
            UUID.randomUUID(),
            List.of()
    );

    // hard-coded token update
    private final Promotion testPromotion = TestSuiteWithPromotion.promotion;
    private final HazelTokenUpdate testTokenUpdate = (HazelTokenUpdate) testPromotion.getZkpTokenUpdates().get(0);
    private final Vector<BigInteger> testEarnAmount = Vector.of(BigInteger.valueOf(12L));

    // shared secret for authenticated queries
    @Value("${incentive-service.provider-secret}")
    private String providerSecret;

    // Use a MockBean to prevent the CryptoRepository from being created (and trying to connect to the info service)
    @MockBean
    private CryptoRepository cryptoRepository;

    // Mock basket repository to inject baskets
    @MockBean
    private BasketRepository basketRepository;

    @MockBean
    private OfflineDSPRepository offlineDSPRepository;
    private ArrayList<Zn.ZnElement> dsids;
    private boolean dsidOffline;


    @BeforeEach
    public void mock(@Autowired WebTestClient webTestClient) {
        // program hard-coded return values for the crypto and basket repositories using mockito
        when(cryptoRepository.getPublicParameters()).thenReturn(pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(pkp.getPk());
        when(cryptoRepository.getProviderSecretKey()).thenReturn(pkp.getSk());
        when(basketRepository.getBasket(testBasket.getBasketId())).thenReturn(testBasket);
        when(basketRepository.isBasketPaid(testBasket.getBasketId())).thenReturn(false);
        when(basketRepository.getBasket(emptyTestBasket.getBasketId())).thenReturn(emptyTestBasket);
        when(basketRepository.isBasketPaid(emptyTestBasket.getBasketId())).thenReturn(false);
        when(basketRepository.getBasket(emptyTestBasketTwo.getBasketId())).thenReturn(emptyTestBasketTwo);
        when(basketRepository.isBasketPaid(emptyTestBasketTwo.getBasketId())).thenReturn(false);

        dsids = new ArrayList<>();
        dsidOffline = false;
        when(offlineDSPRepository.containsDsid(any())).thenAnswer(invocation ->
            dsids.contains(invocation.getArgument(0, GroupElement.class))
        );
        when(offlineDSPRepository.simulatedDosAttackOngoing()).thenAnswer(invocation -> dsidOffline);
        doAnswer(invocation -> {
            var spendRequest = invocation.getArgument(2, SpendRequest.class);
            dsids.add(spendRequest.getDsid());
            return null;
        }).when(offlineDSPRepository).addToDbSyncQueue(any(), any(), any(), any());
        doAnswer(invocation -> {
            dsidOffline=true;
            return null;
        }).when(offlineDSPRepository).addShortWaitPeriod();
        doAnswer(invocation -> {
            dsidOffline=false;
            return null;
        }).when(offlineDSPRepository).removeAllWaitPeriod();

        // clear all promotions for clean test starting state
        deleteAllPromotions(webTestClient, providerSecret, HttpStatus.OK);
    }

    /**
     * Tests functionality for issuing registration tokens to new users.
     */
    @Test
    public void registrationTest(@Autowired WebTestClient webClient) {
        // use hard-coded key pair (from crypto.testFixtures) of a user that is not yet in the system
        var userPreKeyPair = TestSuite.userPreKeyPair;
        var registrationCoupon = TestSuite.incentiveSystem.signRegistrationCoupon(TestSuite.storeKeyPair, TestSuite.userKeyPair.getPk(), "Some User Name");

        // issuing of registration signature
        SPSEQSignature signature = retrieveRegistrationSignatureForCoupon(webClient, registrationCoupon);

        // assert that signature verifies under the providers SPS-EQ key
        assertThat(pp.getSpsEq().verify(pkp.getPk().getRegistrationSpsEqPk(), signature, userPreKeyPair.getPk().getUpk(), pp.getW()))
                .isTrue();
    }

    @Test
    public void registrationCouponStorageTest(@Autowired WebTestClient webClient) {
        var registrationCoupon = TestSuite.incentiveSystem.signRegistrationCoupon(TestSuite.storeKeyPair, TestSuite.userKeyPair.getPk(), "Some User Name");
        retrieveRegistrationSignatureForCoupon(webClient, registrationCoupon);

        var registrationCoupons = getAllRegistrationCoupons(webClient);

        assertThat(registrationCoupons).hasSize(1).anyMatch((coupon) -> registrationCoupon.getUserInfo().equals(coupon.getUserInfo()));
    }

    /**
     * Tests implementation of the Issue algorithm (server side of the issue join protocol).
     */
    @Test
    public void joinTest(@Autowired WebTestClient webClient) {
        // add the promotion used for tests to the system
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);

        // actual Issue execution
        Token token = joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.OK);

        // verify that a token was generated
        assertThat(token).isNotNull();
    }

    /**
     * Verifies that Issue algorithm does not work for a promotion that is not in the system.
     */
    @Test
    public void joinNonExistingPromotionTest(@Autowired WebTestClient webClient) {
        assertThatThrownBy(
                () -> joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.BAD_REQUEST)
        ).hasStackTraceContaining("Promotion");
    }

    @Test
    public void earnTest(@Autowired WebTestClient webClient) {
        // add promotion that is used for tests to the system
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);
        Token token = TestSuite.generateToken(testPromotion.getPromotionParameters());
        EarnStoreCouponSignature earnStoreCouponSignature = TestSuite.getEarnCouponForPromotion(testPromotion.getPromotionParameters(), token, testBasket.getBasketId(), testEarnAmount);

        // generate earn request and pretend like the test user sent it to you
        var updatedToken = earnWithProviderECDSA(
                webClient,
                incentiveSystem,
                pkp,
                ukp,
                token,
                testEarnAmount,
                earnStoreCouponSignature,
                testPromotion.getPromotionParameters()
        );

        assertThat(updatedToken.getPoints().map(RingElement::asInteger)).isEqualTo(testEarnAmount);
    }

    @Test
    public void earnTestOld(@Autowired WebTestClient webClient) {
        // add promotion that is used for tests to the system
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);

        // execute Issue to generate a token for the test promotion for the test user
        Token token = joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.OK);

        // evaluate test basket to determine how many points the user earns
        var pointsToEarn = testPromotion.computeEarningsForBasket(testBasket);

        // generate earn request and pretend like the test user sent it to you
        var earnRequest = generateAndSendEarnRequest(
                webClient,
                incentiveSystem,
                pkp,
                ukp,
                token,
                testPromotion.getPromotionParameters().getPromotionId(),
                testBasket.getBasketId(),
                HttpStatus.OK
        );

        // test basket is considered paid now (-> change this in hard-coded mock repo)
        when(basketRepository.isBasketPaid(testBasket.getBasketId())).thenReturn(true);


        retrieveTokenAfterEarn(
                webClient,
                incentiveSystem,
                testPromotion,
                pkp,
                ukp,
                token,
                earnRequest,
                testBasket.getBasketId(),
                pointsToEarn,
                HttpStatus.OK
        );
    }

    @Test
    public void earnWrongBasketTest(@Autowired WebTestClient webClient) {
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);
        Token token = joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.OK);

        generateAndSendEarnRequest(webClient,
                incentiveSystem,
                pkp,
                ukp,
                token,
                testPromotion.getPromotionParameters().getPromotionId(),
                UUID.randomUUID(),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    public void earnInvalidPromotionIdTest(@Autowired WebTestClient webClient) {
        addPromotion(webClient, testPromotion, providerSecret, HttpStatus.OK);
        Token token = joinPromotion(webClient, incentiveSystem, pkp, ukp, testPromotion, HttpStatus.OK);

        generateAndSendEarnRequest(webClient,
                incentiveSystem,
                pkp,
                ukp,
                token,
                BigInteger.valueOf(14),
                UUID.randomUUID(),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    void spendTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, testPromotion, providerSecret, HttpStatus.OK);
        var tokenPoints = Vector.of(BigInteger.valueOf(35));
        var basketPoints = Vector.of(BigInteger.valueOf(0));
        var pointsAfterSpend = testTokenUpdate.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).orElseThrow();
        var token = Helper.generateToken(
                pp,
                ukp,
                pkp,
                testPromotion.getPromotionParameters(),
                tokenPoints
        );

        SpendRequest spendRequest = sendSingleSpendRequest(webTestClient, basketPoints, pointsAfterSpend, token, emptyTestBasket, HttpStatus.OK);
        when(basketRepository.isBasketPaid(emptyTestBasket.getBasketId())).thenReturn(true);
        retrieveTokenAfterSpend(webTestClient, token, pointsAfterSpend, spendRequest, emptyTestBasket);
    }

    @Test
    void spendWithoutPaymentTest(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, testPromotion, providerSecret, HttpStatus.OK);
        var tokenPoints = Vector.of(BigInteger.valueOf(35));
        var basketPoints = Vector.of(BigInteger.valueOf(0));
        var pointsAfterSpend = testTokenUpdate.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).orElseThrow();
        var token = Helper.generateToken(
                pp,
                ukp,
                pkp,
                testPromotion.getPromotionParameters(),
                tokenPoints
        );

        // transaction shall not be synced into db in this test case
        sendSingleSpendRequest(webTestClient, basketPoints, pointsAfterSpend, token, emptyTestBasket, HttpStatus.OK);

        // Not paid yet
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-update-results").build())
                .header("basket-id", String.valueOf(emptyTestBasket.getBasketId()))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void emptyBulkRequestTest(@Autowired WebTestClient webTestClient) {
        sendBulkRequests(webTestClient, new BulkRequestDto(List.of(), List.of()), emptyTestBasket, HttpStatus.OK); // transaction shall not be synced into DB in this test case
    }

    @Test
    void testDSP_ImmediateRejection(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, testPromotion, providerSecret, HttpStatus.OK);
        var tokenPoints = Vector.of(BigInteger.valueOf(35));
        var basketPoints = Vector.of(BigInteger.valueOf(0));
        var pointsAfterSpend = testTokenUpdate.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).orElseThrow();
        var token = Helper.generateToken(
                pp,
                ukp,
                pkp,
                testPromotion.getPromotionParameters(),
                tokenPoints
        );

        SpendRequest spendRequest = sendSingleSpendRequest(webTestClient, basketPoints, pointsAfterSpend, token, emptyTestBasket, HttpStatus.OK);
        when(basketRepository.isBasketPaid(emptyTestBasket.getBasketId())).thenReturn(true);
        var firstRemainderToken = retrieveTokenAfterSpend(webTestClient, token, pointsAfterSpend, spendRequest, emptyTestBasket);

        // spend token again; should be prevented with I_AM_A_TEAPOT
        sendSingleSpendRequest(webTestClient, basketPoints, pointsAfterSpend, token, emptyTestBasketTwo, HttpStatus.I_AM_A_TEAPOT);
    }

    @Test
    void testDosAttackEnablesDoubleSpendingAttack(@Autowired WebTestClient webTestClient) {
        addPromotion(webTestClient, testPromotion, providerSecret, HttpStatus.OK);
        var tokenPoints = Vector.of(BigInteger.valueOf(35));
        var basketPoints = Vector.of(BigInteger.valueOf(0));
        var pointsAfterSpend = testTokenUpdate.computeSatisfyingNewPointsVector(tokenPoints, basketPoints).orElseThrow();
        var token = Helper.generateToken(
                pp,
                ukp,
                pkp,
                testPromotion.getPromotionParameters(),
                tokenPoints
        );

        SpendRequest spendRequest = sendSingleSpendRequest(webTestClient, basketPoints, pointsAfterSpend, token, emptyTestBasket, HttpStatus.OK);
        when(basketRepository.isBasketPaid(emptyTestBasket.getBasketId())).thenReturn(true);
        var firstRemainderToken = retrieveTokenAfterSpend(webTestClient, token, pointsAfterSpend, spendRequest, emptyTestBasket);

        // DoS should enable double spending attack
        activateDos(webTestClient);

        SpendRequest doubleSpendingRequest = sendSingleSpendRequest(webTestClient, basketPoints, pointsAfterSpend, token, emptyTestBasketTwo, HttpStatus.OK);
        when(basketRepository.isBasketPaid(emptyTestBasketTwo.getBasketId())).thenReturn(true);

        var secondRemainderToken= retrieveTokenAfterSpend(webTestClient, token, pointsAfterSpend, doubleSpendingRequest, emptyTestBasketTwo);

        assertThat(firstRemainderToken.getPoints()).isEqualTo(secondRemainderToken.getPoints());
    }

    private void activateDos(WebTestClient webTestClient) {
        webTestClient.get().uri("/dos/short-duration").exchange().expectStatus().isOk();
    }

    /**
     * Generates and returns a single spend request and sends it to the incentive service using the passed WebTestClient.
     *
     * @param webTestClient the client used for sending the request
     * @param basketPoints vector representing the points that the user can earn for his basket
     * @param pointsAfterSpend remaining points in the token after the spend transaction
     * @param token spent token
     * @param expectedStatus an exception is thrown if the HTTPResponse status code differs from this one
     * @return spend request crypto object
     */
    private SpendRequest sendSingleSpendRequest(WebTestClient webTestClient, Vector<BigInteger> basketPoints, Vector<BigInteger> pointsAfterSpend, Token token, Basket basket, HttpStatus expectedStatus) {
        var metadata = testPromotion.generateMetadataForUpdate();
        var spendDeductTree = testTokenUpdate.generateRelationTree(basketPoints, metadata);
        var tid = basket.getBasketId(pp.getBg().getZn());
        SpendRequest spendRequest = incentiveSystem.generateSpendRequest(
                testPromotion.getPromotionParameters(),
                token,
                pkp.getPk(),
                pointsAfterSpend,
                ukp,
                tid,
                spendDeductTree
        );
        var bulkRequestDto = new BulkRequestDto(
                Collections.emptyList(),
                List.of(new SpendRequestDto(
                        testPromotion.getPromotionParameters().getPromotionId(),
                        testTokenUpdate.getTokenUpdateId(),
                        jsonConverter.serialize(spendRequest.getRepresentation()),
                        jsonConverter.serialize(new RepresentableRepresentation(metadata))
                )));
        sendBulkRequests(webTestClient, bulkRequestDto, basket, expectedStatus);
        return spendRequest;
    }

    /**
     * Sends a bulk of multiple earn and spend requests to the incentive server using the WebTestClient.
     * @param webTestClient the client used to send the request bulk to the server
     * @param bulkRequestDto bulk of Earn and Spend requests
     * @param testBasket basket the user earns points for
     * @param expectedStatus an exception if the HTTPResponse status code differs from this one
     */
    private void sendBulkRequests(WebTestClient webTestClient, BulkRequestDto bulkRequestDto, Basket testBasket, HttpStatus expectedStatus) {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-updates").build())
                .header("basket-id", String.valueOf(testBasket.getBasketId()))
                .body(BodyInserters.fromValue(bulkRequestDto))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    private Token retrieveTokenAfterSpend(WebTestClient webTestClient, Token token, Vector<BigInteger> pointsAfterSpend, SpendRequest spendRequest, Basket basket) {
        var resultsDto = webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/bulk-token-update-results").build())
                .header("basket-id", String.valueOf(basket.getBasketId()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(TokenUpdateResultsDto.class)
                .returnResult().getResponseBody();
        assert resultsDto != null;
        var serializedSpendResponse = resultsDto.getZkpTokenUpdateResultDtoList().get(0).getSerializedResponse();
        SpendResponse spendResponse = new SpendResponse(jsonConverter.deserialize(serializedSpendResponse), pp.getBg().getZn(), pp.getSpsEq());
        return incentiveSystem.handleSpendRequestResponse(testPromotion.getPromotionParameters(), spendResponse, spendRequest, token, pointsAfterSpend, pkp.getPk(), ukp);
    }

    private SPSEQSignature retrieveRegistrationSignatureForCoupon(WebTestClient webClient, RegistrationCoupon registrationCoupon) {
        var serializedSignature = webClient.get()
                .uri("/register-with-coupon")
                .header("registration-coupon", jsonConverter.serialize(registrationCoupon.getRepresentation()))
                .exchange()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        return pp.getSpsEq().restoreSignature(jsonConverter.deserialize(serializedSignature));
    }

    private RegistrationCouponJSON[] getAllRegistrationCoupons(WebTestClient webClient) {
        return webClient.get()
                .uri("/registration-coupons")
                .exchange()
                .expectBody(RegistrationCouponJSON[].class)
                .returnResult()
                .getResponseBody();
    }
}
