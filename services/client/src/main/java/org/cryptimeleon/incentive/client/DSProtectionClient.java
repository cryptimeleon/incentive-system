package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.Representation;
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

    public static final String ADD_TRANSACTION_TOKEN_EDGE_PATH = "/addtatokenedge";
    public static final String ADD_TOKEN_TRANSACTION_EDGE_PATH = "/addtokentaedge";

    public static final String CONTAINS_TRANSACTION_PATH = "/containsta";
    public static final String CONTAINS_TOKEN_PATH = "/containsdsid";

    public static final String CONTAINS_TRANSACTION_TOKEN_EDGE_PATH = "/containstatokenedge";
    public static final String CONTAINS_TOKEN_TRANSACTION_EDGE_PATH = "/containstokentaedge";

    public static final String INVALIDATE_TRANSACTION_PATH = "/invalidateta";

    public static final String ADD_AND_LINK_USER_INFO_PATH = "/adduserinfo";
    public static final String GET_USER_INFO_PATH = "/getUserInfo";

    public static final String RETRIEVE_TRANSACTION_PATH = "/retrieveta";
    public static final String RETRIEVE_ALL_TRANSACTIONS_PATH = "/retrieveallta";

    public DSProtectionClient(String dsProtectionServiceURL) {
        this.dsProtectionClient = WebClientHelper.buildWebClient(dsProtectionServiceURL);
    }

    /**
     * Adds the passed transaction to the database.
     * @return dsprotection database server response
     */
    public String addTransactionNode(Transaction ta, DoubleSpendingTag dsTag){
        // marshall the data as JSON string
        JSONConverter jsonConverter = new JSONConverter();
        String serializedTransactionRepr = jsonConverter.serialize(ta.getRepresentation());
        String serializedDsTagRepr = jsonConverter.serialize(dsTag.getRepresentation());

        // add transaction using POST request to ds protection service using web client from object variable
        Mono<String> addTransactionRequestResponse = this.dsProtectionClient.post() // do a POST request
                .uri(uriBuilder -> uriBuilder.path(ADD_TRANSACTION_PATH).build()) // construct URI the request should go to
                .header("ta", serializedTransactionRepr) // add the transaction to add to the database to the respective header
                .bodyValue(serializedDsTagRepr)
                .retrieve() // actually make the request
                .bodyToMono(String.class); // convert the response body

        // return response
        return addTransactionRequestResponse.block();
    }

    /**
     * Adds the passed double-spending ID to the database.
     * @return dsprotrection database server response
     */
    public String addTokenNode(GroupElement dsid, UserInfo uInfo){
        // marshall the data as JSON string
        JSONConverter jsonConverter = new JSONConverter();
        String serializedDsidRepr = jsonConverter.serialize(dsid.getRepresentation());
        String serialzedUserInfoRepr = jsonConverter.serialize(uInfo.getRepresentation());

        // add double-spending ID using a POST request to ds protection service using web client from object variable
        Mono<String> addDsidRequestResponse = this.dsProtectionClient.post()
                .uri(uriBuilder -> uriBuilder.path(ADD_DSID_PATH).build())
                .header("dsid", serializedDsidRepr)
                .bodyValue(serialzedUserInfoRepr)
                .retrieve()
                .bodyToMono(String.class);

        // return response
        return addDsidRequestResponse.block();
    }

    /**
     * Adds an edge from the transaction with the passed tid and gamma
     * to the token identified via the passed dsid.
     * Semantics of this edge: transaction procuced token
     * @return success or failure report (HTTP response)
     */
    public String addTransactionTokenEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid){
        return this.addEdge(tid, gamma, dsid, ADD_TRANSACTION_TOKEN_EDGE_PATH);
    }

    /**
     * Adds an edge from the token identified via with the passed dsid
     * to the transaction with the passed tid and gamma.
     * Semantics of this edge: token was consumed in transaction
     * @return success or failure report (HTTP response)
     */
    public String addTokenTransactionEdge(GroupElement dsid, Zn.ZnElement tid, Zn.ZnElement gamma){
        return this.addEdge(tid, gamma, dsid, ADD_TOKEN_TRANSACTION_EDGE_PATH);
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

    /**
     * Checks the remote database for containment of an edge
     * between the transaction with the passed tid and gamma
     * and the token with the passed double-spending ID
     * @return whether the passed edge is in the database
     */
    public boolean containsTransactionTokenEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid){
        return this.containsEdge(tid, gamma, dsid, CONTAINS_TRANSACTION_TOKEN_EDGE_PATH);
    }

    /**
     * Checks the remote database for containment of an edge
     * between the token with the passed double-spending ID
     * and the transaction with the passed tid and gamma
     * @return whether the passed edge is in the database
     */
    public boolean containsTokenTransactionEdge(GroupElement dsid, Zn.ZnElement tid, Zn.ZnElement gamma) {
        return this.containsEdge(tid, gamma, dsid, CONTAINS_TOKEN_TRANSACTION_EDGE_PATH);
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

    /**
     * Retrieves the user info associated to the passed dsid from the database.
     * @param pp public parameters of the respective incentive system instance
     *           (needed for restoring the queried user info since server answers with a representation)
     * @return
     */
    public UserInfo getUserInfo(GroupElement dsid, IncentivePublicParameters pp){
        // marshall double-spending ID
        JSONConverter jsonConverter = new JSONConverter();
        String serializedDsidRepr = jsonConverter.serialize(dsid.getRepresentation());

        // make post request to double-spending protection service
        Mono<String> getUserInfoRequestResponse = this.dsProtectionClient.get()
                .uri(uriBuilder -> uriBuilder.path(GET_USER_INFO_PATH).build())
                .header("dsid", serializedDsidRepr)
                .retrieve()
                .bodyToMono(String.class);

        // deserialize user info from answer
        Representation uInfoRepr = jsonConverter.deserialize(getUserInfoRequestResponse.block());
        UserInfo userInfo = new UserInfo(uInfoRepr, pp);

        return userInfo;
    }


    /**
     * helper methods
     */

    /**
     * Adds an edge between the transaction identified with tid and gamma
     * and the token identified with dsid
     * to the remote database.
     * Direction of the edge is specified by an additional parameter.
     * @param edgeTypeAdditionPath the URL path of the dsprotection service endpoint for adding the desired edge type
     *                             (transaction->token or token->transaction),
     *                             respective constants are defined in this class
     * @return HTTP response with status information
     */
    private String addEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid, String edgeTypeAdditionPath) {
        // marshall transaction identifier and dsid
        TransactionIdentifier taIdentifier = new TransactionIdentifier(tid, gamma);
        JSONConverter jsonConverter = new JSONConverter();
        String serializedTaIdRepr = jsonConverter.serialize(taIdentifier.getRepresentation());
        String serializedDsIdRepr = jsonConverter.serialize(dsid.getRepresentation());

        // make post request to dsprotection service
        Mono<String> addEdgeRequestResponse = this.dsProtectionClient.post()
                .uri(uriBuilder -> uriBuilder.path(edgeTypeAdditionPath).build())
                .header("dsid", serializedDsIdRepr)
                .header("taid", serializedTaIdRepr)
                .retrieve()
                .bodyToMono(String.class);

        return addEdgeRequestResponse.block();
    }

    /**
     * Checks remote database for containment of an edge between the transaction identified with tid and gamma
     * and the token identified with dsid.
     * Direction of the edge is specified by an additional parameter.
     * @param edgeTypeContainmentPath the URL path of the dsprotection service endpoint
     *                                for checking database for containment of the desired edge type
     *                                (transaction->token or token->transaction),
     *                                respective constants are defined in this class
     * @return
     */
    private boolean containsEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid, String edgeTypeContainmentPath) {
        // marshall transaction identifier and dsid
        TransactionIdentifier taIdentifier = new TransactionIdentifier(tid, gamma);
        JSONConverter jsonConverter = new JSONConverter();
        String serializedTaIdRepr = jsonConverter.serialize(taIdentifier.getRepresentation());
        String serializedDsIdRepr = jsonConverter.serialize(dsid.getRepresentation());

        // make get request to ds protection service
        Mono<Boolean> containsEdgeResponse = this.dsProtectionClient.get()
                .uri(uriBuilder -> uriBuilder.path(edgeTypeContainmentPath).build())
                .header("taid", serializedTaIdRepr)
                .header("dsid", serializedDsIdRepr)
                .retrieve()
                .bodyToMono(Boolean.class);

        return containsEdgeResponse.block();
    }
}
