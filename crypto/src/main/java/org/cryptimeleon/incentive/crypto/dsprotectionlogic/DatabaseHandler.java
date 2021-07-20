package org.cryptimeleon.incentive.crypto.dsprotectionlogic;

import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import java.math.BigInteger;

// TODO: finalize signatures, currently some of them are wip

/**
 * Provides an interface to the double-spending DB for the DBSync algorithm from the formal cryptographic definition of the incentive system.
 * This comprises methods for adding nodes and edges to it, representing DsIDs, transactions and both relations between them (transaction consumes token, token is result of transaction).
 */
public class DatabaseHandler
{
    private String databaseEndpointURL = ""; // URL requested when adding a transaction, dsID or edge to the database TODO: replace by proper URL

    /**
     * methods for administration of nodes and edges
     */
    public void addTransactionNode(ZnElement tid, ZnElement gamma, BigInteger k, DoubleSpendingTag dstag){
        // marshall the data as a JSON TODO: continue here

        // encode the obtained JSON (Base64URL)

        // make a HTTP request to the double spending protection database service with the encoded transaction as a GET parameter

    }

    public void addTokenNode(GroupElement dsid){
        // marshall the data as a JSON TODO: continue here

        // encode the obtained JSON (Base64URL)

        // make a HTTP request to the double spending protection database service with the encoded DsID as a GET parameter
    }

    // for making an edge from a transaction to a token node
    public void addTransactionTokenEdge(ZnElement tid, ZnElement gamma, GroupElement dsid){

    }

    // for making an edge from a token to a transaction node
    public void addTokenTransactionEdge(GroupElement dsid, ZnElement tid, ZnElement gamma){

    }

    public boolean containsTransactionNode(ZnElement tid, ZnElement gamma){
        return false;
    }

    public boolean containsTokenNode(GroupElement dsid){
        return false;
    }

    public boolean containsEdge(){
        return false;
    }

    /**
     * end of methods for administration of nodes and edges
     */
}
