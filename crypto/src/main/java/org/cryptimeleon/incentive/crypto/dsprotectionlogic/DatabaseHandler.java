package org.cryptimeleon.incentive.crypto.dsprotectionlogic;

import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

// TODO: finalize signatures, currently some of them are wip

/**
 * Provides an interface to the double-spending DB for the DBSync algorithm from the formal cryptographic definition of the incentive system.
 * This comprises methods for adding nodes and edges to it, representing DsIDs, transactions and both relations between them (transaction consumes token, token is result of transaction).
 */

public interface DatabaseHandler
{
    /**
     * methods for administration of nodes and edges
     */
    public void addTransactionNode(Transaction ta);

    public void addTokenNode(GroupElement dsid);

    // for making an edge from a transaction to a token node
    public void addTransactionTokenEdge(ZnElement tid, ZnElement gamma, GroupElement dsid);

    // for making an edge from a token to a transaction node
    public void addTokenTransactionEdge(GroupElement dsid, ZnElement tid, ZnElement gamma);

    public boolean containsTransactionNode(ZnElement tid, ZnElement gamma);

    public boolean containsTokenNode(GroupElement dsid);

    public boolean containsEdge();

    /**
     * end of methods for administration of nodes and edges
     */
}
