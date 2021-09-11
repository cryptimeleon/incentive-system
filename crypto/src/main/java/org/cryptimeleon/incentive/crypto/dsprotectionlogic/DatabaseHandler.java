package org.cryptimeleon.incentive.crypto.dsprotectionlogic;

import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * Provides an interface to the double-spending DB for the DBSync algorithm from the formal cryptographic definition of the incentive system.
 * This comprises methods for adding nodes and edges to it, representing DsIDs, transactions and both relations between them (transaction consumes token, token is result of transaction)
 * as well as nodes for checking for containment of a certain node or edge of any type.
 * Note that transactions are usually identified by the combination of transaction ID tid and challenge generator gamma. Tokens are identified by dsids.
 */

public interface DatabaseHandler
{
    /**
     * Methods for adding nodes and edges.
     * 2 types of nodes: transaction and token nodes (token nodes correspond to dsids)
     * 2 types of edges indicating different relations between tokens and transactions:
     * token -> transaction: token was used in this transaction
     * transaction -> token: transaction produced this token
     */

    public void addTransactionNode(Transaction ta);

    public void addTokenNode(GroupElement dsid);

    // for making an edge from a transaction to a token node
    public void addTransactionTokenEdge(ZnElement tid, ZnElement gamma, GroupElement dsid);

    // for making an edge from a token to a transaction node
    public void addTokenTransactionEdge(GroupElement dsid, ZnElement tid, ZnElement gamma);

    /**
     * end of methods for adding nodes and edges
     */

    /**
     * Methods for checking for containment of nodes and edges.
     */

    public boolean containsTransactionNode(ZnElement tid, ZnElement gamma);

    public boolean containsTokenNode(GroupElement dsid);

    public boolean containsTransactionTokenEdge(ZnElement tid, ZnElement gamma, GroupElement dsid);

    public boolean containsTokenTransactionEdge(GroupElement dsid, ZnElement tid, ZnElement gamma);

    /**
     * end of methods for containment checks
     */

    /**
     * methods for handling user info associated to a token
     */

    /**
     * Adds info about the user that spend a specific token in a specific transaction to said token.
     * @param tid ID of transaction
     * @param gamma challenge generator for transaction
     * @param upk public key of user
     * @param dsBlame dsBlame info for the transaction
     * @param dsTrace dsTrace info for the transaction
     */
    public void addUserInfo(ZnElement tid, ZnElement gamma, UserPublicKey upk, ZnElement dsBlame, ZnElement dsTrace);

    /**
     * Returns the user info associated to the transaction identified by tid and gamma.
     * @param tid ID of the transaction
     * @param gamma challenge generator of the transaction
     * @return user info
     */
    public UserInfo getUserInfo(ZnElement tid, ZnElement gamma);

    /**
     * end of methods handling user info associated to a token
     */

    /**
     * Marks the transaction specified by the passed ID and challenge generator as invalid.
     * @param tid ID of the transaction
     * @param gamma challenge generator of the transaction
     */
    public void invalidateTransaction(ZnElement tid, ZnElement gamma);
}
