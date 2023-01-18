package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * This repository encapsulates the cryptographic assets/objects.
 * </br>
 * It connects to the info-service and queries the public parameters and provider keys using an authenticated request
 * and retries MAX_TRIES times, each time doubling the waiting time.
 */
@Repository
public class CryptoRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CryptoRepository.class);
    private static final int MAX_TRIES = 5;
    private IncentiveSystem incentiveSystem;
    private IncentivePublicParameters publicParameters;
    private ProviderSecretKey providerSecretKey;
    private ProviderPublicKey providerPublicKey;
    @Value("${provider.shared-secret}")
    private String sharedSecret; // used to authenticate the request for the provider secret key (set via environment variable)
    // Will be set via dependency injection

    @Value("${spring.profiles.active}")
    private String activeProfiles;

    private final InfoClient infoClient;

    @Autowired
    private CryptoRepository(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    /**
     * Make sure that the shared secret is set.
     */
    @PostConstruct
    public void validateValue() {
        // Do not run this in unit-tests
        if (activeProfiles.contains("test")) return;

        log.info("PostConstruct");
        if (sharedSecret.equals("")) {
            throw new IllegalArgumentException("Provider shared secret is not set!");
        }
        log.info("shared secret: {}", sharedSecret);
        init();
    }

    /**
     * Initialize repository by querying assets from info service and parsing them into java objects.
     */
    private void init() {
        log.info("Querying configuration from info service");
        JSONConverter jsonConverter = new JSONConverter();
        // Try several times, each time waiting for 2^i seconds before retrying.
        for (int i = 0; i < MAX_TRIES; i++) {
            try {
                log.info("Trying to query data from info service. Attempt " + i);
                String serializedPublicParameters = infoClient.querySerializedPublicParameters().block(Duration.ofSeconds(1));
                String serializedProviderPublicKey = infoClient.querySerializedProviderPublicKey().block(Duration.ofSeconds(1));
                String serializedProviderSecretKey = infoClient.querySerializedProviderSecretKey(sharedSecret).block(Duration.ofSeconds(1));
                this.publicParameters = new IncentivePublicParameters(jsonConverter.deserialize(serializedPublicParameters));
                this.providerPublicKey = new ProviderPublicKey(jsonConverter.deserialize(serializedProviderPublicKey), publicParameters);
                this.providerSecretKey = new ProviderSecretKey(jsonConverter.deserialize(serializedProviderSecretKey), publicParameters);
                this.incentiveSystem = new IncentiveSystem(publicParameters);
                break;
            } catch (RuntimeException e) {
                if (i + 1 == MAX_TRIES) {
                    e.printStackTrace();
                    throw new RuntimeException("Could not query data from info service!");
                }
            }
            try {
                Thread.sleep((long) (1000 * Math.pow(2, i)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public IncentiveSystem getIncentiveSystem() {
        return this.incentiveSystem;
    }

    public IncentivePublicParameters getPublicParameters() {
        return this.publicParameters;
    }

    public ProviderSecretKey getProviderSecretKey() {
        return this.providerSecretKey;
    }

    public ProviderPublicKey getProviderPublicKey() {
        return this.providerPublicKey;
    }
}
