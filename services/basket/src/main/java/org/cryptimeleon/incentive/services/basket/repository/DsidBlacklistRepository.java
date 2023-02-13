package org.cryptimeleon.incentive.services.basket.repository;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class DsidBlacklistRepository implements IDsidBlacklistHandler {

    private final HashMap<Zn.ZnElement, Zn.ZnElement> storage = new HashMap<>();

    @Override
    public boolean containsDsidWithDifferentGamma(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma) {
        return storage.containsKey(doubleSpendingId) && storage.get(doubleSpendingId).equals(gamma);
    }

    @Override
    public void addEntryIfDsidNotPresent(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma) {
        storage.putIfAbsent(doubleSpendingId, gamma);
    }
}
