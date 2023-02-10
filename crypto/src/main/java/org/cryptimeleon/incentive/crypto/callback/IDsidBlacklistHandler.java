package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Blacklist spend requests with different hash while allowing re-requesting with same hash gamma.
 */
public interface IDsidBlacklistHandler {
    boolean containsDsidWithDifferentGamma(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma);

    void addEntryIfDsidNotPresent(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma);
}
