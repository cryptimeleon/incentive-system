package org.cryptimeleon.incentivesystem.cryptoprotocol.doublespending;

import org.cryptimeleon.math.structures.rings.zn.Zn;

public interface DatabaseHandler
{
    // TODO: this is only a mock interface, signatures are highly wip
    public void addNode(Zn.ZnElement tid);

    public void addEdge();

    public boolean containsNode();

    public boolean containsEdge();
}
