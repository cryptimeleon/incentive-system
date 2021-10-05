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
 * Methods of this class can be mapped 1:1 to the server-side request mappers in the deduct package.
 * Communication is done via POST requests, data objects are transferred as JSON-marshalled representations (see Representation in math package).
 */
public class DSProtectionClient implements DatabaseHandler {
    private WebClient dsProtectionClient; // the underlying web client making the requests

    public static final String ADD_TRANSACTION_PATH = "/addtransaction";
    public static final String ADD_DSID_PATH = "/adddsid";

    public static final String CONTAINS_TRANSACTION_PATH = "/containsta";
    public static final String CONTAINS_TOKEN_PATH = "/containsdsid";

    public static final String INVALIDATE_TRANSACTION_PATH = "/invalidateta";

    public static final String ADD_AND_LINK_USER_INFO_PATH = "/adduserinfo";

    public static final String RETRIEVE_TRANSACTION_PATH = "/retrieveta";
    public static final String RETRIEVE_ALL_TRANSACTIONS_PATH = "/retrieveallta";

    public DSProtectionClient(String dsProtectionServiceURL) {
        this.dsProtectionClient = WebClientHelper.buildWebClient(dsProtectionServiceURL);
    }

    /**
     * Adds the passed transaction to the database.
     * @return dsprotection database server response
     */
    public String addTransactionNode(Transaction ta){
        // marshall the data as JSON string
        JSONConverter jsonConverter = new JSONConverter();
        String serializedTransactionRepr = jsonConverter.serialize(ta.getRepresentation());

        // add transaction using POST request to ds protection service using web client from object variable
        Mono<String> addTransactionRequestResponse = this.dsProtectionClient.post() // do a POST request
                .uri(uriBuilder -> uriBuilder.path(ADD_TRANSACTION_PATH).build()) // construct URI the request should go to
                .bodyValue(serializedTransactionRepr) // add the transaction to add to the database to the body
                .retrieve() // actually make the request
                .bodyToMono(String.class); // convert the response body

        // return response
        return addTransactionRequestResponse.block();
    }

    /**
     * Adds the passed double-spending ID to the database.
     * @return dsprotrection database server response
     */
    public String addTokenNode(GroupElement dsid){
        // marshall the data as JSON string
        JSONConverter jsonConverter = new JSONConverter();
        String serializedDsidRepr = jsonConverter.serialize(dsid.getRepresentation());

        // add double-spending ID using a POST request to ds protection service using web client from object variable
        Mono<String> addDsidRequestResponse = this.dsProtectionClient.post()
                .uri(uriBuilder -> uriBuilder.path(ADD_DSID_PATH).build())
                .bodyValue(serializedDsidRepr)
                .retrieve()
                .bodyToMono(String.class);

        // return response
        return addDsidRequestResponse.block();
    }

    // for making an edge from a transaction to a token node
    public String addTransactionTokenEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid){
        return "";
    }

    // for making an edge from a token to a transaction node
    public String addTokenTransactionEdge(GroupElement dsid, Zn.ZnElement tid, Zn.ZnElement gamma){
        return "";
    }

    /**
     * Checks the database for containment of a transaction with the passed transaction ID and challenge generator gamma.
     * @return whether such a transaction is contained in the database
     */
    public boolean containsTransactionNode(Zn.ZnElement tid, Zn.ZnElement gamma){
        // wrap up the transaction-identifying data and marshall it as a JSON string
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(tid, gamma);
        String serializedTaIdentifierRepr = jsonConverter.serialize(taIdentifier.getRepresentation());

        // make respective get request to ds protection service
        Mono<Boolean> isContained = this.dsProtectionClient.get()
                .uri(uriBuilder -> uriBuilder.path(CONTAINS_TRANSACTION_PATH).build())
                .header("taidgamma", serializedTaIdentifierRepr)
                .retrieve()
                .bodyToMono(Boolean.class);

        // return result
        return isContained.block();
    }

    /**
     * Checks the database for containment of the passed double-spending ID.
     * @return whether passed dsid is contained in the database
     */
    public boolean containsTokenNode(GroupElement dsid){
        // marshall the dsid as a JSON string
        JSONConverter jsonConverter = new JSONConverter();
        String serializedDsidRepr = jsonConverter.serialize(dsid.getRepresentation());

        // make respective get request to ds protection service
        Mono<Boolean> isContained = this.dsProtectionClient.get()
                .uri(uriBuilder -> uriBuilder.path(CONTAINS_TOKEN_PATH).build())
                .header("dsid", serializedDsidRepr)
                .retrieve()
                .bodyToMono(Boolean.class);

        // return response
        return isContained.block();
    }

    public boolean containsTransactionTokenEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid){
        return false;
    }

    public boolean containsTokenTransactionEdge(GroupElement dsid, Zn.ZnElement tid, Zn.ZnElement gamma) {
        return false;
    }

    /**
     * Invalidates the transaction with the passed transaction ID and challenge generator gamma.
     * @return HTTP response body stating whether deletion successful
     */
    public String invalidateTransaction(Zn.ZnElement tid, Zn.ZnElement gamma) {
        // marshall transaction identifier
        JSONConverter jsonConverter = new JSONConverter();
        TransactionIdentifier taIdentifier = new TransactionIdentifier(tid, gamma);
        String serializedTransactionIdentifierRepr = jsonConverter.serialize(taIdentifier.getRepresentation());

        // make post request to double-spending protection service
        Mono<String> invalidationRequestResponse = this.dsProtectionClient.post()
                .uri(uriBuilder -> uriBuilder.path(INVALIDATE_TRANSACTION_PATH).build())
                .bodyValue(serializedTransactionIdentifierRepr)
                .retrieve()
                .bodyToMono(String.class);

        // return response
        return invalidationRequestResponse.block();
    }

    /**
     * Adds user info (the passed user public key, dsblame and dstrace) to the database and links it to the token
     * with the passed double-spending ID.
     * @return HTTP response body stating whether adding and linking was successful
     */
    public String addAndLinkUserInfo(UserPublicKey upk, Zn.ZnElement dsBlame, Zn.ZnElement dsTrace, GroupElement dsid) {
        // marshall user info and double-spending ID
        JSONConverter jsonConverter = new JSONConverter();
        UserInfo uInfo = new UserInfo(upk, dsBlame, dsTrace);
        String serializedUserInfoRepr = jsonConverter.serialize(uInfo.getRepresentation());
        String serializedDsidRepr = jsonConverter.serialize(dsid.getRepresentation());

        // make post request to double-spending protection service
        Mono<String> addAndLinkRequestResponse = this.dsProtectionClient.post()
                .uri(uriBuilder -> uriBuilder.path(ADD_AND_LINK_USER_INFO_PATH).build())
                .header("dsid", serializedDsidRepr)
                .bodyValue(serializedUserInfoRepr)
                .retrieve()
                .bodyToMono(String.class);

        // return response
        return addAndLinkRequestResponse.block();
    }

    public UserInfo getUserInfo(GroupElement dsid){
        return null; // TODO
    }
}
