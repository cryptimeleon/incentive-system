package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

/**
 * Store (double-spending-id, hash) tuples to prevent double-spending attacks but enable retries (same hash).
 */
@Repository
public class DsidBlacklistRepository implements IDsidBlacklistHandler {
    HashMap<Zn.ZnElement, Zn.ZnElement> dsMap = new HashMap<>();

    @Override
    public boolean containsDsidWithDifferentGamma(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma) {
        return dsMap.containsKey(doubleSpendingId) && !dsMap.get(doubleSpendingId).equals(gamma);
    }

    @Override
    public void addEntryIfDsidNotPresent(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma) {
        dsMap.putIfAbsent(doubleSpendingId, gamma);
    }
}
