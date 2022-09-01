package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class FakeDSPRepository implements OfflineDSPRepository {
    public void addToDbSyncQueue(BigInteger promotionId, Zn.ZnElement tid, SpendRequest spendRequest, DeductOutput spendProviderOutput) {

    }

    public boolean dspServiceIsAlive() {
        return false;
    }

    public boolean containsDsid(GroupElement dsid) {
        return false;
    }
}
