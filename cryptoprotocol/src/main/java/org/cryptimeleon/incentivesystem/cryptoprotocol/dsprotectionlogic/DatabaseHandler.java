package org.cryptimeleon.incentivesystem.cryptoprotocol.dsprotectionlogic;

import org.cryptimeleon.incentivesystem.cryptoprotocol.model.DoubleSpendingTag;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import java.math.BigInteger;

/**
 * Provides an interface to the double-spending DB for the DBSync algorithm from the formal cryptographic definition of incentive system.
 */
public interface DatabaseHandler
{
    /**
     * methods for administration of nodes and edges
     */

    // TODO: this is only a mock interface, signatures are wip
    public void addTransactionNode(ZnElement tid, ZnElement gamma, BigInteger spendAmount, DoubleSpendingTag dsTag);

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
