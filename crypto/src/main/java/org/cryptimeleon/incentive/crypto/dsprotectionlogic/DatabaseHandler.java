package org.cryptimeleon.incentive.crypto.dsprotectionlogic;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import java.math.BigInteger;
import java.util.Base64;

// TODO: finalize signatures, currently some of them are wip

/**
 * Provides an interface to the double-spending DB for the DBSync algorithm from the formal cryptographic definition of the incentive system.
 * This comprises methods for adding nodes and edges to it, representing DsIDs, transactions and both relations between them (transaction consumes token, token is result of transaction).
 */
@Value
public class DatabaseHandler
{
    private String databaseEndpointURL; // URL requested when adding a transaction, dsID or edge to the database, passed as constructor parameter

    /**
     * methods for administration of nodes and edges
     */
    public void addTransactionNode(Transaction ta){
        // marshall the data as a JSON
        String jsonTransaction = new Gson().toJson(ta);
        System.out.println(jsonTransaction);

        // encode the obtained JSON (Base64URL)
        String encodedJsonTransaction = Base64.getUrlEncoder().encodeToString(jsonTransaction.getBytes());
        System.out.println(encodedJsonTransaction);

        // make a HTTP request to the double spending protection database service with the encoded transaction as a GET parameter
        // TODO
    }

    public void addTokenNode(GroupElement dsid){
        // marshall the data as a JSON
        String jsonDsid = new Gson().toJson(dsid);

        // encode the obtained JSON (Base64URL)
        String encodedJsonDsid = Base64.getUrlEncoder().encodeToString(jsonDsid.getBytes());

        // make a HTTP request to the double spending protection database service with the encoded DsID as a GET parameter
        // TODO
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
