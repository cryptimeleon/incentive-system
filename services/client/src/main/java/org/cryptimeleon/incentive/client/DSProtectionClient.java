package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;


/**
 * Implements the connectivity to the double-spending protection database.
 * Transaction data is sent via POST requests, data objects are transferred as JSON-marshalled representations (see Representation in math package).
 */
public class DSProtectionClient {
    private Logger logger = LoggerFactory.getLogger(DSProtectionClient.class);

    private WebClient dsProtectionClient; // the underlying web client making the requests

    private static final String DBSYNC_PATH = "/dbsync";
    private static final String CLEAR_DB_PATH = "/cleardb";
    private static final String GET_TRANSACTION_PATH = "/getta";

    public DSProtectionClient(String dsProtectionServiceURL) {
        logger.info("Creating a client that sends queries to " + dsProtectionServiceURL);
        this.dsProtectionClient = WebClientHelper.buildWebClient(dsProtectionServiceURL);
    }

    /**
     * Sends a request to the double-spending protection service to add the passed (represented and serialized) transaction data to the database.
     *
     * @param tid         transaction ID
     * @param dsid        double-spending ID
     * @param dstag       double-spending tag
     * @param userChoice  represents the reward that the user claimed with this transaction
     * @return server response (success or failure report)
     */
    public String dbSync(Zn.ZnElement tid, GroupElement dsid, DoubleSpendingTag dstag, BigInteger promotionId, String userChoice) {
        // marshall transaction data
        String serializedTid = Helper.computeSerializedRepresentation(tid);
        String serializedDsidRepr = Helper.computeSerializedRepresentation(dsid);
        String serializedDsTagRepr = Helper.computeSerializedRepresentation(dstag);
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
     * @param taIdentifier transaction identifier, consisting of a numerical ID and the challenge generator gamma
     * @return Transaction object (crypto)
     */
    public Transaction getTransaction(TransactionIdentifier taIdentifier) {
        // marshall transaction identifier data
        String serializedTransactionIdentifier = Helper.computeSerializedRepresentation(taIdentifier);

        // make request
        Mono<Transaction> getTransactionResponse = this.dsProtectionClient.get()
                .uri(uriBuilder -> uriBuilder.path(GET_TRANSACTION_PATH).build())
                .header("taidentifier", serializedTransactionIdentifier)
                .retrieve()
                .bodyToMono(Transaction.class);

        // return result
        return getTransactionResponse.block();
    }
}
