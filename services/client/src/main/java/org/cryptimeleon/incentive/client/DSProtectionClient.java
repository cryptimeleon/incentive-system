package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.time.Duration;


/**
 * Implements the connectivity to the double-spending protection database.
 * Transaction data is sent via POST requests, data objects are transferred as JSON-marshalled representations (see Representation in math package).
 */
public class DSProtectionClient {
    private static final String DBSYNC_PATH = "/dbsync";
    private static final String CLEAR_DB_PATH = "/cleardb";
    private static final String GET_TRANSACTION_PATH = "/getta";
    private static final String CONTAINS_DSID_PATH = "/containsdsid";
    private static final String HEARTBEAT_PATH = "/";
    private static final long HEARTBEAT_TIMEOUT = 3600; // how long to wait before considering the dsp service down
    private Logger logger = LoggerFactory.getLogger(DSProtectionClient.class);
    private WebClient dsProtectionClient; // the underlying web client making the requests



    public DSProtectionClient(String dsProtectionServiceURL) {
        logger.info("Creating a client that sends queries to " + dsProtectionServiceURL);
        this.dsProtectionClient = WebClientHelper.buildWebClient(dsProtectionServiceURL);
    }

    /**
     * Sends a request to the double-spending protection service to add the passed (represented and serialized) transaction data to the database.
     *
     * @param tid        transaction ID
     * @param dsid       double-spending ID
     * @param dstag      double-spending tag
     * @param userChoice represents the reward that the user claimed with this transaction
     * @return server response (success or failure report)
     */
    public String dbSync(Zn.ZnElement tid, GroupElement dsid, DoubleSpendingTag dstag, BigInteger promotionId, String userChoice) {
        // marshall transaction data
        String serializedTid = computeSerializedRepresentation(tid);
        String serializedDsidRepr = computeSerializedRepresentation(dsid);
        String serializedDsTagRepr = computeSerializedRepresentation(dstag);
        String serializedPromotionId = promotionId.toString();

        // make POST request
        Mono<String> dbSyncResponse = this.dsProtectionClient.post()
                .uri(uriBuilder -> uriBuilder.path(DBSYNC_PATH).build())
                .header("tid", serializedTid)
                .header("dsid", serializedDsidRepr)
                .header("dstag", serializedDsTagRepr)
                .header("promotion-id", serializedPromotionId)
                .header("userchoice", userChoice)
                .retrieve()
                .bodyToMono(String.class);

        // return response
        return dbSyncResponse.block();
    }

    /**
     * Causes the double-spending protection service to clear all databases
     *
     * @return response text
     */
    public String clearDatabase() {
        Mono<String> response = this.dsProtectionClient.post()
                .uri(uriBuilder -> uriBuilder.path(CLEAR_DB_PATH).build())
                .retrieve()
                .bodyToMono(String.class);

        return response.block();
    }

    /**
     * Returns the transaction with the specified transaction identifier from the database if contained.
     *
     * @param taIdentifier transaction identifier, consisting of a numerical ID and the challenge generator gamma
     * @return Transaction object (crypto)
     */
    public String getTransaction(TransactionIdentifier taIdentifier) {
        // marshall transaction identifier data
        String serializedTransactionIdentifier = computeSerializedRepresentation(taIdentifier);

        // make request and return result
        return this.dsProtectionClient.get()
                .uri(uriBuilder -> uriBuilder.path(GET_TRANSACTION_PATH).build())
                .header("taidentifier", serializedTransactionIdentifier)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Returns true if and only if token with the specified double-spending protection ID is already contained in the database.
     */
    public Boolean containsDsid(GroupElement dsid) {
        // marshall data
        String serializedDsidRepr = computeSerializedRepresentation(dsid);

        /*
         * Make request and return result if present.
         */
        return this.dsProtectionClient.get()
                .uri(uriBuilder -> uriBuilder.path(CONTAINS_DSID_PATH).build())
                .header("dsid", serializedDsidRepr)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    /**
     * Performs a heartbeat check for the double-spending protection service.
     * I.e. returns true if and only if the double-spending protection service is up and running.
     */
    public Boolean dspServiceIsAlive(){
        String heartbeatResponse = null;

        // make request to heartbeat endpoint, timeout leads to RuntimeException
        try {
            heartbeatResponse = this.dsProtectionClient.get()
                    .uri(uriBuilder -> uriBuilder.path(HEARTBEAT_PATH).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(HEARTBEAT_TIMEOUT));
        }
        catch (RuntimeException re) {
            // if previous request times out: dsp service is down
            return false;
        }

        // if received response: return true, else false
        return heartbeatResponse != null;
    }


    /**
     * Helper method that computes a serialized representation of the passed representable r.
     */
    private static String computeSerializedRepresentation(Representable r) {
        JSONConverter jsonConverter = new JSONConverter();
        return jsonConverter.serialize(
                r.getRepresentation()
        );
    }
}
