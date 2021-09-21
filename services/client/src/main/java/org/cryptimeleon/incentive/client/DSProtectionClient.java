package org.cryptimeleon.incentive.client;

import lombok.Value;
import org.cryptimeleon.incentive.crypto.dsprotectionlogic.DatabaseHandler;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;


/**
 * Implements the connectivity to the double-spending protection database.
 * This comprises methods for adding transaction and token nodes to a bipartite graph and both types of directed edges.
 */
@Value
public class DSProtectionClient implements DatabaseHandler {
    private String databaseEndpointURL; // protocol, domain, port of the URL requested when adding a transaction, dsID or edge to the database, passed as constructor parameter

    public void addTransactionNode(Transaction ta){
        // marshall the data as JSON string TODO: switch to JSONConverter from math
        JSONConverter jsonConverter = new JSONConverter();
        String serializedTransactionRepr = jsonConverter.serialize(ta.getRepresentation());

        // make POST request to ds protection service using WebClient

    }

    public void addTokenNode(GroupElement dsid){

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
