package org.cryptimeleon.incentive.crypto.dsprotectionlogic;

import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.util.ArrayList;

/**
 * Provides an interface to the double-spending DB for the DBSync algorithm from the formal cryptographic definition of the incentive system.
 * This comprises methods for adding nodes and edges to it, representing DsIDs, transactions and both relations between them (transaction consumes token, token is result of transaction)
 * as well as nodes for checking for containment of a certain node or edge of any type.
 * Note that transactions are usually identified by the combination of transaction ID tid and challenge generator gamma. Tokens are identified by dsids.
 */

public interface DatabaseHandler {
    /*
     * Methods for adding nodes and edges.
     * 2 types of nodes: transaction and token nodes (token nodes correspond to dsids)
     * 2 types of edges indicating different relations between tokens and transactions:
     * token -> transaction: token was used in this transaction
     * transaction -> token: transaction produced this token
     */

    /**
     * Adds a new transaction node to the database.
     *
     * @param ta transaction to add
     */
    void addTransactionNode(Transaction ta);

    Transaction getTransactionNode(TransactionIdentifier taId);

    void addTokenNode(GroupElement dsid);

    // for making an edge from a transaction to a token node
    void addTransactionTokenEdge(TransactionIdentifier taId, GroupElement dsid);

    // for making an edge from a token to a transaction node
    void addTokenTransactionEdge(GroupElement dsid, TransactionIdentifier taId);



    /*
     * end of methods for adding nodes and edges
     */

    /*
     * Methods for checking for containment of nodes and edges.
     */


    boolean containsTransactionNode(TransactionIdentifier taIdentifier);

    boolean containsTokenNode(GroupElement dsid);

    boolean containsTransactionTokenEdge(TransactionIdentifier taId, GroupElement dsid);

    boolean containsTokenTransactionEdge(GroupElement dsid, TransactionIdentifier taId);



    /*
     * end of methods for containment checks
     */



    /*
     * methods for handling user info associated to a token
     */


    /**
     * Adds info about the user that spent a specific token to said token.
     *
     * @param userInfo user info
     * @param dsid     double-spending ID identifying the token
     */
    void addAndLinkUserInfo(UserInfo userInfo, GroupElement dsid);

    /**
     * Retrieves the user info associated to the passed double-spending ID.
     */
    UserInfo getUserInfo(GroupElement dsid);



    /*
     * end of methods handling user info associated to a token
     */


    /**
     * Retrieves all transactions that have consumed the passed double-spending ID.
     */
    ArrayList<Transaction> getConsumingTransactions(GroupElement dsid);

    /**
     * Retrieves the double-spending ID of the token that was consumed in the transaction with the passed identifier.
     *
     * @return the double-spending id
     */
    GroupElement getConsumedTokenDsid(TransactionIdentifier taId);


    /**
     * Marks the transaction specified by the passed ID and challenge generator as invalid.
     */
    void invalidateTransaction(TransactionIdentifier taIdentifier);

    /**
     * Helper methods providing info about the database state.
     * Note that they are designed to not expose any information about the underlying database administration objects (like for example CRUDRepositories).
     */
    long getTransactionCount();

    long getTokenCount();

    long getDsTagCount();

    long getUserInfoCount();
}
