package org.cryptimeleon.incentive.services.provider;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.exception.StoreDoubleSpendingDetectedException;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.promotion.TestSuiteWithPromotion;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.services.provider.api.RegistrationCouponJSON;
import org.cryptimeleon.incentive.services.provider.repository.CryptoRepository;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.cryptimeleon.incentive.services.provider.ClientHelper.*;
import static org.mockito.Mockito.when;

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
public class ProviderServiceTest {
    // public parameters, incentive system and key pairs from crypto.testFixtures
    private static final IncentivePublicParameters pp = TestSuite.pp;
    private static final IncentiveSystem incentiveSystem = TestSuite.incentiveSystem;

    // JSON converter for marshalling/de-marshalling of query data
    private static final JSONConverter jsonConverter = new JSONConverter();

    private final Vector<BigInteger> testEarnAmount = Vector.of(BigInteger.valueOf(12L));

    // shared secret for authenticated queries
    @Value("${incentive-service.provider-secret}")
    private String providerSecret;

    // Use a MockBean to prevent the CryptoRepository from being created (and trying to connect to the info service)
    @MockBean
    private CryptoRepository cryptoRepository;

    private static Token generateToken() {
        return Helper.generateToken(
                pp,
                TestSuiteWithPromotion.userKeyPair,
                TestSuiteWithPromotion.providerKeyPair,
                TestSuiteWithPromotion.promotion.getPromotionParameters(),
                TestSuiteWithPromotion.pointsBeforeSpend
        );
    }

    private static Token retrieveUpdatedTokenFromResponse(Token token, SpendProviderRequest spendProviderRequest, SpendProviderResponse spendProviderResponse) {
        return incentiveSystem.retrieveUpdatedTokenFromSpendResponse(
                TestSuiteWithPromotion.userKeyPair,
                TestSuiteWithPromotion.providerKeyPair.getPk(),
                token,
                TestSuiteWithPromotion.promotion.getPromotionParameters(),
                TestSuiteWithPromotion.pointsAfterSpend,
                spendProviderRequest,
                spendProviderResponse);
    }

    private static SpendProviderResponse runSpendWithService(WebTestClient webClient, SpendProviderRequest spendProviderRequest) {
        return runSpendWithService(webClient, spendProviderRequest, TestSuiteWithPromotion.basket);
    }

    private static SpendProviderResponse runSpendWithService(WebTestClient webClient, SpendProviderRequest spendProviderRequest, Basket basket) {
        return spend(webClient,
                incentiveSystem,
                TestSuiteWithPromotion.promotion.getPromotionParameters(),
                TestSuiteWithPromotion.spendTokenUpdate,
                spendProviderRequest,
                TestSuiteWithPromotion.metadata,
                basket.getBasketId(),
                TestSuiteWithPromotion.basketPoints.toList());
    }

    private static SpendStoreResponse generateSpendCouponSignature(SpendStoreRequest spendStoreRequest) throws StoreDoubleSpendingDetectedException {
        return generateSpendCouponSignature(spendStoreRequest, TestSuiteWithPromotion.basket);
    }

    private static SpendStoreResponse generateSpendCouponSignature(SpendStoreRequest spendStoreRequest, Basket basket) throws StoreDoubleSpendingDetectedException {
        return incentiveSystem.signSpendCoupon(
                TestSuiteWithPromotion.storeKeyPair,
                TestSuiteWithPromotion.providerKeyPair.getPk(),
                basket.getBasketId(),
                TestSuiteWithPromotion.promotion.getPromotionParameters(),
                spendStoreRequest,
                TestSuiteWithPromotion.tree,
                TestSuiteWithPromotion.context,
                (basketId, promotionId, hash) -> IStoreBasketRedeemedHandler.BasketRedeemState.BASKET_NOT_REDEEMED,
                new TestSuite.TestDsidBlacklist(),
                spendTransactionData -> {}
        );
    }

    private static SpendStoreRequest generateSpendCouponRequest(Token token) {
        return generateSpendCouponRequest(token, TestSuiteWithPromotion.basket);
    }

    private static SpendStoreRequest generateSpendCouponRequest(Token token, Basket basket) {
        return incentiveSystem.generateStoreSpendRequest(
                TestSuiteWithPromotion.userKeyPair,
                TestSuiteWithPromotion.providerKeyPair.getPk(),
                token,
                TestSuiteWithPromotion.promotion.getPromotionParameters(),
                basket.getBasketId(),
                TestSuiteWithPromotion.pointsAfterSpend,
                TestSuiteWithPromotion.tree,
                TestSuiteWithPromotion.context);
    }

    @BeforeEach
    public void mock(@Autowired WebTestClient webTestClient) {
        // program hard-coded return values for the crypto and basket repositories using mockito
        when(cryptoRepository.getPublicParameters()).thenReturn(pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(TestSuite.providerKeyPair.getPk());
        when(cryptoRepository.getProviderSecretKey()).thenReturn(TestSuite.providerKeyPair.getSk());
        when(cryptoRepository.getProviderKeyPair()).thenReturn(TestSuite.providerKeyPair);

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
        assertThat(pp.getSpsEq().verify(TestSuite.providerKeyPair.getPk().getRegistrationSpsEqPk(), signature, userPreKeyPair.getPk().getUpk(), pp.getW()))
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
        addPromotion(webClient, TestSuiteWithPromotion.promotion, providerSecret, HttpStatus.OK);

        // actual Issue execution
        Token token = joinPromotion(webClient, incentiveSystem, TestSuite.providerKeyPair, TestSuite.userKeyPair, TestSuiteWithPromotion.promotion, HttpStatus.OK);

        // verify that a token was generated
        assertThat(token).isNotNull();
    }

    /**
     * Verifies that Issue algorithm does not work for a promotion that is not in the system.
     */
    @Test
    public void joinNonExistingPromotionTest(@Autowired WebTestClient webClient) {
        assertThatThrownBy(
                () -> joinPromotion(webClient, incentiveSystem, TestSuite.providerKeyPair, TestSuite.userKeyPair, TestSuiteWithPromotion.promotion, HttpStatus.BAD_REQUEST)
        ).hasStackTraceContaining("Promotion");
    }

    @Test
    public void earnTest(@Autowired WebTestClient webClient) {
        // add promotion that is used for tests to the system
        addPromotion(webClient, TestSuiteWithPromotion.promotion, providerSecret, HttpStatus.OK);
        Token token = TestSuite.generateToken(TestSuiteWithPromotion.promotion.getPromotionParameters());
        EarnStoreResponse earnStoreResponse = TestSuite.getEarnCouponForPromotion(token, testEarnAmount, TestSuiteWithPromotion.basket.getBasketId(), TestSuiteWithPromotion.promotion.getPromotionParameters().getPromotionId());

        // generate earn request and pretend like the test user sent it to you
        var updatedToken = earn(
                webClient,
                incentiveSystem,
                TestSuite.providerKeyPair,
                TestSuite.userKeyPair,
                token,
                testEarnAmount,
                earnStoreResponse,
                TestSuiteWithPromotion.promotion.getPromotionParameters()
        );

        assertThat(updatedToken.getPoints().map(RingElement::asInteger)).isEqualTo(testEarnAmount);
    }

    @Test
    public void spendTestECDSA(@Autowired WebTestClient webClient) throws StoreDoubleSpendingDetectedException {
        addPromotion(webClient, TestSuiteWithPromotion.promotion, providerSecret, HttpStatus.OK);

        var token = generateToken();
        SpendStoreRequest spendStoreRequest = generateSpendCouponRequest(token);
        SpendStoreResponse spendCouponSignature = generateSpendCouponSignature(spendStoreRequest);
        SpendProviderRequest spendProviderRequest = new SpendProviderRequest(spendStoreRequest, spendCouponSignature);

        // Request
        SpendProviderResponse spendProviderResponse = runSpendWithService(webClient, spendProviderRequest);

        Token updatedToken = retrieveUpdatedTokenFromResponse(token, spendProviderRequest, spendProviderResponse);
        assertThat(updatedToken.getPoints().map(RingElement::asInteger)).isEqualTo(TestSuiteWithPromotion.pointsAfterSpend);
    }

    @Test
    public void spendTestTwiceECDSA(@Autowired WebTestClient webClient) throws StoreDoubleSpendingDetectedException {
        addPromotion(webClient, TestSuiteWithPromotion.promotion, providerSecret, HttpStatus.OK);

        var token = generateToken();
        SpendStoreRequest spendStoreRequest = generateSpendCouponRequest(token);
        SpendStoreResponse spendCouponSignature = generateSpendCouponSignature(spendStoreRequest);
        SpendProviderRequest spendProviderRequest = new SpendProviderRequest(spendStoreRequest, spendCouponSignature);

        SpendStoreRequest spendStoreRequest2 = generateSpendCouponRequest(token, TestSuiteWithPromotion.basketButWithDifferentId);
        SpendStoreResponse spendCouponSignature2 = generateSpendCouponSignature(spendStoreRequest2, TestSuiteWithPromotion.basketButWithDifferentId);
        SpendProviderRequest spendProviderRequest2 = new SpendProviderRequest(spendStoreRequest2, spendCouponSignature2);

        // Request
        SpendProviderResponse spendProviderResponse = runSpendWithService(webClient, spendProviderRequest);
        retrieveUpdatedTokenFromResponse(token, spendProviderRequest, spendProviderResponse);

        // TODO this is not super nice, expect a specific error code!
        assertThatThrownBy(() -> runSpendWithService(webClient, spendProviderRequest2, TestSuiteWithPromotion.basketButWithDifferentId)).isInstanceOf(AssertionError.class);
    }

    @Test
    public void spendTestInvalidECDSA(@Autowired WebTestClient webClient) throws StoreDoubleSpendingDetectedException {
        addPromotion(webClient, TestSuiteWithPromotion.promotion, providerSecret, HttpStatus.OK);

        var token = generateToken();
        SpendStoreRequest spendStoreRequest = generateSpendCouponRequest(token);
        SpendStoreResponse spendCouponSignature = generateSpendCouponSignature(spendStoreRequest);
        var manipulatedSpendCouponRequest = new SpendStoreRequest(
                spendStoreRequest.getDsid().add(pp.getBg().getZn().valueOf(1)),
                spendStoreRequest.getC(),
                spendStoreRequest.getSigma(),
                spendStoreRequest.getC0(),
                spendStoreRequest.getCPre0(),
                spendStoreRequest.getCPre1(),
                spendStoreRequest.getSpendZkp()
        );
        SpendProviderRequest spendProviderRequest = new SpendProviderRequest(manipulatedSpendCouponRequest, spendCouponSignature);

        // TODO this is not super nice, expect a specific error!
        assertThatThrownBy(() -> runSpendWithService(webClient, spendProviderRequest)).isInstanceOf(AssertionError.class);
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
