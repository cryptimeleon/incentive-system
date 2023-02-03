package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DSPRepository {
    private final Set<Zn.ZnElement> dsidSet = new HashSet<>();

    public DSPRepository() {}

    // TODO here is some work that needs to be done soon!

    /**
     * Returns true if and only if the double-spending database contains a node for the passed dsid.
     */
    public boolean containsDsid(Zn.ZnElement dsid) {
        return dsidSet.contains(dsid);
    }
}
