package org.cryptimeleon.incentive.services.incentive.repository;

import java.time.LocalDateTime;

/**
 * Allows to temporarily suspend functionality of the system.
 */
public interface CyclingScheduler {
    void addShortWaitPeriod();

    void addLongWaitPeriod();

    void removeAllWaitPeriod();

    LocalDateTime getWaitUntil();
}
