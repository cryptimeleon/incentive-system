package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


/**
 * Implements the connectivity to the double-spending protection database.
 * This comprises methods for adding transaction and token nodes to a bipartite graph and both types of directed edges.
 */
public class DSProtectionClient implements DatabaseHandler {
    private WebClient dsProtectionClient; // the underlying web client making the requests

    public static final String ADD_TRANSACTION_PATH = "/addtransaction";

    public static final String CONTAINS_TRANSACTION_PATH = "/containsta";

    public static final String RETRIEVE_TRANSACTION_PATH = "/retrieveta";
    public static final String RETRIEVE_ALL_TRANSACTIONS_PATH = "/retrieveallta";

    public DSProtectionClient(String dsProtectionServiceURL) {
        this.dsProtectionClient = WebClientHelper.buildWebClient(dsProtectionServiceURL);
    }

    public String addTransactionNode(Transaction ta){
        // marshall the data as JSON string
        JSONConverter jsonConverter = new JSONConverter();
        String serializedTransactionRepr = jsonConverter.serialize(ta.getRepresentation());

        // add transaction using POST request to ds protection service using web client from object variable
        Mono<String> addTransactionRequestResponse = dsProtectionClient.post() // do a POST request
                .uri(uriBuilder -> uriBuilder.path(ADD_TRANSACTION_PATH).build()) // construct URI the request should go to
                .bodyValue(serializedTransactionRepr) // add the transaction to add to the database to the body
                .retrieve() // actually make the request
                .bodyToMono(String.class); // convert the response body

        // return response
        return addTransactionRequestResponse.toString();
    }

    public void addTokenNode(GroupElement dsid){
        // marshall the data as JSON string

        // add double-spending ID using a POST request to ds protection service using web client from object variable

        // return response

    }

    // for making an edge from a transaction to a token node
    public void addTransactionTokenEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid){

    }

    // for making an edge from a token to a transaction node
    public void addTokenTransactionEdge(GroupElement dsid, Zn.ZnElement tid, Zn.ZnElement gamma){

    }

    public boolean containsTransactionNode(Zn.ZnElement tid, Zn.ZnElement gamma){
        // wrap up the transaction-identifying data and marshall it as a JSON string
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(tid, gamma);
        String serializedTaIdentifierRepr = jsonConverter.serialize(taIdentifier.getRepresentation());

        // make respective get request to ds protection service
        Mono<Boolean> isContained = dsProtectionClient.get()
                .uri(uriBuilder -> uriBuilder.path(CONTAINS_TRANSACTION_PATH).build())
                .header("taidgamma", serializedTaIdentifierRepr)
                .retrieve()
                .bodyToMono(Boolean.class);

        return false; // TODO how to get boolean value out of the Mono object?
    }

    public boolean containsTokenNode(GroupElement dsid){
        return false;
    }

    public boolean containsTransactionTokenEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid){
        return false;
    }

    public boolean containsTokenTransactionEdge(GroupElement dsid, Zn.ZnElement tid, Zn.ZnElement gamma) {
        return false;
    }

    public void invalidateTransaction(Zn.ZnElement tid, Zn.ZnElement gamma) {
        // update corresponding record in the table
    }

    public void addUserInfo(Zn.ZnElement tid, Zn.ZnElement gamma, UserPublicKey upk, Zn.ZnElement dsBlame, Zn.ZnElement dsTrace) {
        // TODO
    }

    public UserInfo getUserInfo(Zn.ZnElement tid, Zn.ZnElement gamma){
        return null; // TODO
    }
}
