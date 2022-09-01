package org.cryptimeleon.incentive.services.incentive.repository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Repository
public class ScheduledOfflineDSPRepository implements OfflineDSPRepository, CyclingScheduler {
    private static final int DB_SYNC_QUEUE_CYCLE_DELAY = 2000;
    private static final int SHORT_WAIT_PERIOD_SECONDS = 30;
    private static final int LONG_WAIT_PERIOD_SECONDS = 3600;

    private final DSProtectionClient dsProtectionClient; // object handling the connectivity to the double-spending protection database
    private final List<DbSyncTask> taskQueue = Collections.synchronizedList(new ArrayList<>());

    @Getter
    private LocalDateTime waitUntil = LocalDateTime.now().plus(Duration.ofSeconds(5));

    @Autowired
    public ScheduledOfflineDSPRepository(DSProtectionClient dsProtectionClient) {
        this.dsProtectionClient = dsProtectionClient;
    }

    /*
    * OfflineDSPRepository methods
    */

    @Override
    public void addToDbSyncQueue(BigInteger promotionId, Zn.ZnElement tid, SpendRequest spendRequest, DeductOutput spendProviderOutput) {
        DbSyncTask dbSyncTask = new DbSyncTask(promotionId, tid, spendRequest, spendProviderOutput);
        synchronized (taskQueue) {
            taskQueue.add(dbSyncTask);
        }
    }

    @Override
    public boolean dspServiceIsAlive() {
        return dsProtectionClient.dspServiceIsAlive();
    }

    @Override
    public boolean containsDsid(GroupElement dsid) { return dsProtectionClient.containsDsid(dsid); }

    /*
    * end of OfflineDSPRepository methods
    */

    /*
    * CyclingScheduler methods
    */

    @Override
    public void addShortWaitPeriod() {
        waitUntil = LocalDateTime.now().plus(Duration.ofSeconds(SHORT_WAIT_PERIOD_SECONDS));
    }

    @Override
    public void addLongWaitPeriod() {
        waitUntil = LocalDateTime.now().plus(Duration.ofSeconds(LONG_WAIT_PERIOD_SECONDS));
    }

    @Override
    public void removeAllWaitPeriod() {
        waitUntil = LocalDateTime.now().minus(Duration.ofSeconds(1));
    }

    /*
    * end of CyclingScheduler methods
    */

    @Scheduled(fixedDelay = DB_SYNC_QUEUE_CYCLE_DELAY)
    private void scheduleFixedDelayTask() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(waitUntil)) {
            log.info("Executing all tasks in queue");
            synchronized (taskQueue) {
                // TODO make this a batch operation and let dsp service handle the collection of transaction?
                Iterator<DbSyncTask> i = taskQueue.listIterator();
                while (i.hasNext()) {
                    DbSyncTask task = i.next();
                    triggerDbSync(task.getPromotionId(), task.getTid(), task.getSpendRequest(), task.getDeductOutput());
                    i.remove();
                }
            }
        } else {
            log.info("Waiting until " + waitUntil.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    private void triggerDbSync(BigInteger promotionId, Zn.ZnElement tid, SpendRequest spendRequest, DeductOutput spendProviderOutput) {
        dsProtectionClient.dbSync(
                tid,
                spendRequest.getDsid(),
                spendProviderOutput.getDstag(),
                promotionId,
                tid.toString() // TODO change this once user choice generation is properly implemented
        );
    }
}
