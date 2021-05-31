package org.cryptimeleon.incentivesystem.cryptoprotocol.dsprotection;

import org.cryptimeleon.incentivesystem.cryptoprotocol.model.DoubleSpendingTag;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import java.math.BigInteger;

public interface DatabaseHandler
{
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
     * Called upon detection of a double-spending attempt.
     * Marks respective transaction (tid, gamma) invalid and traces transactions resulting from (tid, gamma) using remainder tokens.
     * Implements most of the double-spending protection mechanism from the paper.
     */
    public void traceDoubleSpending();
}
