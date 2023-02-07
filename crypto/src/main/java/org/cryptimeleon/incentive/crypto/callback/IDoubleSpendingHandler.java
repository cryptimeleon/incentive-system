package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.DoubleSpendingDbEntry;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Optional;

public interface IDoubleSpendingHandler {
    boolean containsDsid(Zn.ZnElement dsid);
    Optional<DoubleSpendingDbEntry> getEntryForDsid(Zn.ZnElement dsid);
    void addEntry(Zn.ZnElement dsid, DoubleSpendingDbEntry doubleSpendingDbEntry);
}
