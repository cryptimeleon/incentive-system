package org.cryptimeleon.incentive.crypto.benchmark;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.math.structures.rings.zn.Zn;

public class BenchmarkBlacklist implements IDsidBlacklistHandler {

    @Override
    public boolean containsDsidWithDifferentGamma(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma) {
        return false;
    }

    @Override
    public void addEntryIfDsidNotPresent(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma) {

    }
}
