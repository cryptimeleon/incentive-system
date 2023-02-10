package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Blacklist spend requests with different hash while allowing re-requesting with same hash gamma.
 */
public interface IDsidBlacklistHandler {
    /**
     * Returns true if there is an entry with same dsid and different gamma in the dsid blacklist
     */
    boolean containsDsidWithDifferentGamma(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma);

    /**
     * Add a dsid, gamma tuple to the blacklist. Is ignored if there is already some entry for this dsid.
     */
    void addEntryIfDsidNotPresent(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma);
}
