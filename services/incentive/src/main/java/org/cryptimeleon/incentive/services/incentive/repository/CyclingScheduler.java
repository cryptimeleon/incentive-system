package org.cryptimeleon.incentive.services.incentive.repository;

import java.time.LocalDateTime;

public interface CyclingScheduler {
    void addShortWaitPeriod();

    void addLongWaitPeriod();

    void removeAllWaitPeriod();

    LocalDateTime getWaitUntil();
}
