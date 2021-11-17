package org.cryptimeleon.incentivesystem.dsprotectionservice;

import lombok.Getter;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.Duration;

/**
 * Encapsulates all cryptographic assets/objects that the double-spending protection service needs.
 * This includes the public parameters of the underlying incentive system.
 */
@Repository
public class CryptoRepository {
    public static final int MAX_TRIES = 5; // number of tries that the repo should reconnect to the info service for querying the assets

    private final Logger logger = LoggerFactory.getLogger(CryptoRepository.class);

    @Getter
    private IncentivePublicParameters pp;

    private final InfoClient infoClient; // reference to the object handling the queries to the info service, set via dependency injection ("autowired") mechanism of Spring Boot

    /**
     * Default constructor to be executed when an object of this class is used as a Spring Bean.
     * @param infoClient ref to object handling info service queries
     */
    @Autowired
    public CryptoRepository(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    /**
     * Initializes the repository by querying the required assets from the info service.
     */
    @PostConstruct
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

                logger.info("Deserializing retrieved values.");

                // deserializing retrieved values
                this.pp = new IncentivePublicParameters(jsonConverter.deserialize(serializedPublicParameters));
                break; // if values were received and deserialized successfully, no more attempts are needed
            }
            catch (Exception e) {
                // exceptions are caught and ignored until the final connection attempt has failed
                if(i + 1 >= MAX_TRIES) {
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
