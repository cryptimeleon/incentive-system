package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.services.incentive.repository.CyclingScheduler;
import org.cryptimeleon.incentive.services.incentive.repository.OfflineDSPRepository;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Used for incentive service tests.
 * Mocks the double-spending protection database with an array list of seen double-spending IDs.
 * Provides the same API as the scheduled offline dsp repository used in production
 * by implementing the OfflineDSPRepository and CyclingScheduler interfaces.
 *
 * Only keeps track of the dsids that should be synced into the database
 * (since nothing more is needed for the incentive service tests).
 *
 * For simplicity, it does not distinguish between short and long DoS periods
 * but just keeps a DoS ongoing until it is manually withdrawn.
 */
public class FakeScheduledOfflineDSPRepository implements OfflineDSPRepository, CyclingScheduler {
    private boolean simulatedDosOngoing;
    private ArrayList<GroupElement> dsidList;

    /*
    * OfflineDSPRepository methods
    */

    public void addToDbSyncQueue(BigInteger promotionId, Zn.ZnElement tid, SpendRequest spendRequest, DeductOutput deductOutput) {
        dsidList.add(spendRequest.getDsid());
    }

    public boolean simulatedDosAttackOngoing() { return simulatedDosOngoing; }

    public boolean containsDsid(GroupElement dsid) {
        return dsidList.contains(dsid);
    }

    /*
    * CyclingScheduler methods
    */

    public void addShortWaitPeriod() { this.simulatedDosOngoing = true; }

    // distinction not needed for test cases
    public void addLongWaitPeriod() { this.addShortWaitPeriod(); }

    public void removeAllWaitPeriod() { this.simulatedDosOngoing = false; }

    // not needed for tests, just for semantical correctness of interface implementation
    public LocalDateTime getWaitUntil() { return null; }
}
