package org.cryptimeleon.incentive.crypto.dsprotectionlogic;

import org.cryptimeleon.incentive.crypto.model.Transaction;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * Provides an interface to the double-spending DB for the DBSync algorithm from the formal cryptographic definition of the incentive system.
 * This comprises methods for adding nodes and edges to it, representing DsIDs, transactions and both relations between them (transaction consumes token, token is result of transaction)
 * as well as nodes for checking for containment of a certain node or edge of any type.
 */

public interface DatabaseHandler
{
    public void addTransactionNode(Transaction ta);

    public void addTokenNode(GroupElement dsid);

    // for making an edge from a transaction to a token node
    public void addTransactionTokenEdge(ZnElement tid, ZnElement gamma, GroupElement dsid);

    // for making an edge from a token to a transaction node
    public void addTokenTransactionEdge(GroupElement dsid, ZnElement tid, ZnElement gamma);

    public boolean containsTransactionNode(ZnElement tid, ZnElement gamma);

    public boolean containsTokenNode(GroupElement dsid);

    public boolean containsTransactionTokenEdge(ZnElement tid, ZnElement gamma, GroupElement dsid);

    public boolean containsTokenTransactionEdge(GroupElement dsid, ZnElement tid, ZnElement gamma);
}
