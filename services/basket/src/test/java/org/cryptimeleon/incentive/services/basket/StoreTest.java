package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.services.basket.repository.CryptoRepository;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StoreTest {

    @MockBean
    CryptoRepository cryptoRepository;

    @BeforeAll
    void addTestItems(@Autowired WebTestClient webTestClient) {}

    @BeforeEach
    public void mock(@Autowired WebTestClient webTestClient) {
        // program hard-coded return values for the crypto and basket repositories using mockito
        when(cryptoRepository.getPublicParameters()).thenReturn(TestSuite.pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(TestSuite.incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(TestSuite.providerKeyPair.getPk());
        when(cryptoRepository.getStorePublicKey()).thenReturn(TestSuite.storeKeyPair.getPk());
        when(cryptoRepository.getStoreSecretKey()).thenReturn(TestSuite.storeKeyPair.getSk());
        when(cryptoRepository.getStoreKeyPair()).thenReturn(TestSuite.storeKeyPair);
    }

    @Test
    void registerUserTest(@Autowired WebTestClient webTestClient) {
        UserKeyPair userKeyPair = TestSuite.userKeyPair;
        JSONConverter jsonConverter = new JSONConverter();
        String userInfo = "Some Test User";

        String serializedRegistrationCoupon = webTestClient.get()
                .uri("/register-user-and-obtain-serialized-registration-coupon")
                .header("user-public-key", jsonConverter.serialize(userKeyPair.getPk().getRepresentation()))
                .header("user-info", userInfo)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        RegistrationCoupon registrationCoupon = new RegistrationCoupon(jsonConverter.deserialize(serializedRegistrationCoupon), TestSuite.incentiveSystemRestorer);

        assertThat(TestSuite.incentiveSystem.verifyRegistrationCoupon(registrationCoupon, (s) -> true)).isTrue();
    }
}
