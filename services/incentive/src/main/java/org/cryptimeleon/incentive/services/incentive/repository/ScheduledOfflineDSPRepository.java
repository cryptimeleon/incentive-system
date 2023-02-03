package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
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

@Repository
public class ScheduledOfflineDSPRepository implements OfflineDSPRepository {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScheduledOfflineDSPRepository.class);
    private static final int DB_SYNC_QUEUE_CYCLE_DELAY = 2000;
    private static final int SHORT_WAIT_PERIOD_SECONDS = 30;
    private static final int LONG_WAIT_PERIOD_SECONDS = 3600;
    private final DSProtectionClient dsProtectionClient; // object handling the connectivity to the double-spending protection database
    private final List<DbSyncTask> taskQueue = Collections.synchronizedList(new ArrayList<>());
    private LocalDateTime waitUntil = LocalDateTime.now().plus(Duration.ofSeconds(5));

    @Autowired
    public ScheduledOfflineDSPRepository(DSProtectionClient dsProtectionClient) {
        this.dsProtectionClient = dsProtectionClient;
    }

    /*
    * OfflineDSPRepository methods
    */
    /**
     * Adds a new transaction with the passed information to the queue of transactions to be added to the double-spending database as soon as possible.
     * Uses a List as the data structure to simulate the queue of transactions to be synced into the database.
     *
     * @param promotionId ID of the promotion that the transaction exploits
     * @param tid ID of the transaction
     * @param spendRequest object describing spend request that lead to the transaction
     * @param deductOutput output (= spend response + double-spending tag) that the provider generated when processing the above spend request
     */
    @Override
    public void addToDbSyncQueue(BigInteger promotionId, Zn.ZnElement tid, SpendRequest spendRequest, DeductOutput deductOutput) {
        DbSyncTask dbSyncTask = new DbSyncTask(promotionId, tid, spendRequest, deductOutput);
        synchronized (taskQueue) {
            taskQueue.add(dbSyncTask);
        }
    }

    /**
     * Returns true if and only if the double-spending database contains a node for the passed dsid.
     */
    @Override
    public boolean containsDsid(Zn.ZnElement dsid) {
        return dsProtectionClient.containsDsid(dsid);
    }

    /**
     * Returns true if and only if a simulated DoS attack is currently ongoing.
     */
    @Override
    public boolean simulatedDosAttackOngoing() {
        return waitUntil.isAfter(LocalDateTime.now());
    }

    /*
    * end of OfflineDSPRepository methods
    */
    /*
    * CyclingScheduler methods
    */
    /**
     * Blocks syncing of Spend transactions into the double-spending database for a short period of time.
     */
    @Override
    public void addShortWaitPeriod() {
        waitUntil = LocalDateTime.now().plus(Duration.ofSeconds(SHORT_WAIT_PERIOD_SECONDS));
    }

    /**
     * Blocks syncing of Spend transactions into the double-spending database for a longer period of time.
     */
    @Override
    public void addLongWaitPeriod() {
        waitUntil = LocalDateTime.now().plus(Duration.ofSeconds(LONG_WAIT_PERIOD_SECONDS));
    }

    /**
     * Immediately resumes periodic synchronization of Spend transactions.
     */
    @Override
    public void removeAllWaitPeriod() {
        waitUntil = LocalDateTime.now().minus(Duration.ofSeconds(1));
    }

    /*
    * end of CyclingScheduler methods
    */
    /**
     * Empties queue of waiting db sync tasks (i.e. transactions that need to be synchronized into the double-spending database) if possible
     * by syncing them into the database via a REST endpoint of the double-spending protection service.
     * Called periodically.
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
                    triggerDbSync(task.getPromotionId(), task.getTid(), task.getSpendRequest(), task.getDeductOutput()); // internally makes a REST call
                    i.remove();
                }
            }
        } else {
            log.info("Waiting until " + waitUntil.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    /**
     * Synchronizes a single transaction into the double-spending database.
     * All properties of the transaction are passed as individual parameters.
     */
    private void triggerDbSync(BigInteger promotionId, Zn.ZnElement tid, SpendRequest spendRequest, DeductOutput spendProviderOutput) {
        dsProtectionClient.dbSync(tid, spendRequest.getDsid(), spendProviderOutput.getDstag(), promotionId, tid.toString() // TODO change this once user choice generation is properly implemented
        );
    }

    public LocalDateTime getWaitUntil() {
        return this.waitUntil;
    }
}
