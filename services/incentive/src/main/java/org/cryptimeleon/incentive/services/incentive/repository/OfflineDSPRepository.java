package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * Repository that maintains a queue of Spend transactions that still need to be synced into the double-spending protection database.
 * Provides endpoint to tell whether the double-spending protection service is currently experiencing DoS.
 * <p>
 * Also allows to check if a certain dsid is already contained in the database. This allows for online double-spending protection in a way that
 * the incentive service can abort transactions that spend a token with a dsid that was already spent (i.e. is contained in the database).
 */
public interface OfflineDSPRepository {
    /**
     * Adds a new transaction with the passed information to the queue of transactions to be added to the double-spending database as soon as possible.
     *
     * @param promotionId  ID of the promotion that the transaction exploits
     * @param tid          ID of the transaction
     * @param spendRequest object describing spend request that lead to the transaction
     * @param deductOutput output (= spend response + double-spending tag) that the provider generated when processing the above spend request
     */
    void addToDbSyncQueue(BigInteger promotionId, Zn.ZnElement tid, SpendRequest spendRequest, DeductOutput deductOutput);

    /**
     * Returns true if and only if a simulated DoS attack is currently ongoing.
     */
    boolean simulatedDosAttackOngoing();

    /**
     * Returns true if and only if the double-spending database contains a node for the passed dsid.
     */
    boolean containsDsid(Zn.ZnElement dsid);

    void addShortWaitPeriod();

    void addLongWaitPeriod();

    void removeAllWaitPeriod();

    LocalDateTime getWaitUntil();
}
