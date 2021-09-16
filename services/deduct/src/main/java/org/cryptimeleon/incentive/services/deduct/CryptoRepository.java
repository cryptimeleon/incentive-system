package org.cryptimeleon.incentive.services.deduct;

import lombok.Getter;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.Duration;


/**
 * Encapsulates all cryptographic assets/objects that the provider needs in order to provide the deduct service.
 * Connects to the info service and queries the public parameters of the respective incentive system instance as well as the provider's provider key pair.
 * This query is realized by an authenticated request which is retried MAX_TRIES times, each time with twice the waiting time.
 */
@Repository
public class CryptoRepository {
    public static final int MAX_TRIES=1; // number of tries the repo should reconnect to the info service upon failure

    private Logger logger = LoggerFactory.getLogger(CryptoRepository.class);

    @Getter
    private IncentiveSystem incentiveSystem;

    @Getter
    private IncentivePublicParameters pp; // TODO: better retrieved using getter than dedicated field?

    @Getter
    private ProviderSecretKey sk;

    @Getter
    private ProviderPublicKey pk;

    @Value("${provider.shared-secret}")
    private String sharedSecret; // used to authenticate the request for the provider secret key (set via environment variable) // TODO: set this up in the docker compose file

    private InfoClient infoClient; // reference to the object handling the queries to the info service, set via dependency injection ("autowired") mechanism of Spring Boot

    /**
     * Default constructor to be executed when an object of this class is used as a Spring Bean.
     * @param infoClient ref to object handling info service queries
     */
    @Autowired
    private CryptoRepository(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    /**
     * Ensures that the shared secret is set (exception thrown if not).
     * Then initializes the repository by connecting to the info service and querying the necessary values.
     */
    @Profile("!test")
    @PostConstruct
    public void validateAndInit() {
        logger.info("Validating shared secret");
        if(sharedSecret.equals("")) {
            throw new IllegalArgumentException("Provider shared secret is not set.");
        }
        init();
    }

    /**
     * Initializes the repository by querying the required assets from the info service.
     */
    private void init() {
        logger.info("Querying info service for cryptographic assets for provider.");
        JSONConverter jsonConverter = new JSONConverter(); // info service provides assets as JSON objects => converter needed to obtain java objects

        // several connection attempts, wait 2^i seconds before retrying
        for(int i = 0; i < MAX_TRIES; i++) {
            // attempt to connect to the info service and deserialize responses
            try {
                logger.info("Retrieving data from info service (attempt " + i + ").");

                // retrieving serialized values
                String serializedPublicParameters = this.infoClient.querySerializedPublicParameters().block(Duration.ofSeconds(1));
                String serializedProviderPublicKey = this.infoClient.querySerializedProviderPublicKey().block(Duration.ofSeconds(1));
                String serializedProviderSecretKey = this.infoClient.querySerializedProviderSecretKey(this.sharedSecret).block(Duration.ofSeconds(1));

                logger.info("Deserializing retrieved values.");

                // deserializing retrieved values
                this.pp = new IncentivePublicParameters(jsonConverter.deserialize(serializedPublicParameters));
                this.pk = new ProviderPublicKey(jsonConverter.deserialize(serializedProviderPublicKey), pp.getSpsEq(), pp.getBg().getG1());
                this.sk = new ProviderSecretKey(jsonConverter.deserialize(serializedProviderSecretKey), pp.getSpsEq(), pp.getBg().getZn(), pp.getPrfToZn());
                this.incentiveSystem = new IncentiveSystem(pp);
                break; // if values were received and deserialized successfully, no more attempts are needed
            }
            catch (Exception e) {
                // exceptions are caught and ignored until the final connection attempt has failed
                if(i + 1 > MAX_TRIES) {
                    e.printStackTrace();
                }
            }

            // waiting
            try{
                Thread.sleep((long) (1000 * Math.pow(2, i)));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
