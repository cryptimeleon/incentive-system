package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.services.incentive.repository.CryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.Mockito.*;

/**
 * Tests all the functionality of the incentive service.
 * This includes the server side of the crypto protocols (Issue-Join, Credit-Earn, Spend-Deduct)
 * and the issuing of genesis tokens.
 *
 * Uses a WebTestClient object as the client for the crypto protocol tests.
 * The servers that the incentive service communicates with
 * (namely basket, info and double-spending protection service)
 * are mocked using hard-coded answers to the test queries.
 *
 * The incentive system instance used for all tests is the hard-coded one from the crypto.testFixtures package.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StoreServiceTest {
    // public parameters, incentive system and key pairs from crypto.testFixtures
    private static final IncentivePublicParameters pp = TestSuite.pp;
    private static final IncentiveSystem incentiveSystem = TestSuite.incentiveSystem;
    private static final ProviderKeyPair pkp = TestSuite.providerKeyPair;
    private static final UserKeyPair ukp = TestSuite.userKeyPair;
    @Value("${incentive-service.provider-secret}")
    private String providerSecret;

    // Use a MockBean to prevent the CryptoRepository from being created (and trying to connect to the info service)
    @MockBean
    private CryptoRepository cryptoRepository;

    @BeforeEach
    public void mock(@Autowired WebTestClient webTestClient) {
        // program hard-coded return values for the crypto and basket repositories using mockito
        when(cryptoRepository.getPublicParameters()).thenReturn(pp);
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem);
        when(cryptoRepository.getProviderPublicKey()).thenReturn(pkp.getPk());
        when(cryptoRepository.getProviderSecretKey()).thenReturn(pkp.getSk());
    }

    /**
     * Tests functionality for issuing genesis tokens to new users.
     */
    @Test
    public void registrationTest(@Autowired WebTestClient webClient) {
    }
}
