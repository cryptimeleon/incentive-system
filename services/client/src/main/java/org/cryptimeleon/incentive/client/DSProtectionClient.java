package org.cryptimeleon.incentive.client;

import com.google.gson.Gson;
import lombok.Value;
import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;


/**
 * Implements the connectivity to the double-spending protection database.
 * This comprises methods for adding transaction and token nodes to a bipartite graph and both types of directed edges.
 */
@Value
public class DSProtectionClient implements DatabaseHandler {
    private String databaseEndpointURL; // protocol, domain, port of the URL requested when adding a transaction, dsID or edge to the database, passed as constructor parameter

    public void addTransactionNode(Transaction ta){
        // marshall the data as a JSON TODO: switch to JSONConverter from math
        String jsonTransaction = new Gson().toJson(ta);
        System.out.println(jsonTransaction);

        // encode the obtained JSON (Base64URL)
        String encodedJsonTransaction = Base64.getUrlEncoder().encodeToString(jsonTransaction.getBytes());
        System.out.println(encodedJsonTransaction);

        // TODO: "you shouldnt use GET to alter state (-> CSRF, ...)" -> how to handle POST requests with Spring? see Issue and CreditController
        // compute the URL corresponding to the addition of the respective transaction node (node info is URL parameter)
        String additionURL = databaseEndpointURL + "/addta?encodedta=" + encodedJsonTransaction;

        // make a HTTP request to the double spending protection database service with the encoded transaction as a GET parameter
        HttpClient client = HttpClient.newHttpClient(); // create client object using factory method
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(additionURL))
                .build(); // creates a HTTP request object using builder pattern
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
        }
        catch (IOException e){
            System.out.println("IOException while sending transaction addition request to double-spending protection database service: " + e.getMessage());
        }
        catch(InterruptedException e){
            System.out.println("InterruptedException while sending transaction addition request to double-spending protection database service: " + e.getMessage());
        }
    }

    public void addTokenNode(GroupElement dsid){
        // marshall the data as a JSON TODO: switch to JSONConverter from math
        String jsonDsid = new Gson().toJson(dsid);

        // encode the obtained JSON (Base64URL)
        String encodedJsonDsid = Base64.getUrlEncoder().encodeToString(jsonDsid.getBytes());

        // make a HTTP request to the double spending protection database service with the encoded DsID as a GET parameter
        // TODO
    }

    // for making an edge from a transaction to a token node
    public void addTransactionTokenEdge(Zn.ZnElement tid, Zn.ZnElement gamma, GroupElement dsid){

    }

    // for making an edge from a token to a transaction node
    public void addTokenTransactionEdge(GroupElement dsid, Zn.ZnElement tid, Zn.ZnElement gamma){

    }

    public boolean containsTransactionNode(Zn.ZnElement tid, Zn.ZnElement gamma){
        return false;
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
