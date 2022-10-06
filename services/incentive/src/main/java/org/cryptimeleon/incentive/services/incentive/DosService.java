package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.incentive.services.incentive.repository.ScheduledOfflineDSPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service that allows to simulate denial-of-service attacks on the double-spending protection service
 * by putting the synchronization of transactions into the database on hold for a short period of time.
 * Accessed by the incentive service (i.e. the functionality for DoS on the dsp service is not exposed as HTTP endpoints).
 */
@Service
public class DosService {

    private final ScheduledOfflineDSPRepository offlineDSPRepository;

    @Autowired
    public DosService(ScheduledOfflineDSPRepository offlineDSPRepository) {
        this.offlineDSPRepository = offlineDSPRepository;
    }

    /**
     * Simulates short DoS attack on double-spending protection service.
     */
    public void addShortWaitPeriod() {
        offlineDSPRepository.addShortWaitPeriod();
    }

    /**
     * Simulates long DoS attack on double-spending protection service.
     */
    public void addLongWaitPeriod() {
        offlineDSPRepository.addLongWaitPeriod();
    }

    /**
     * Ends all simulated DoS attacks on the double-spending protection service.
     */
    public void removeAllWaitPeriod() {
        offlineDSPRepository.removeAllWaitPeriod();
    }

    /**
     * Returns the remaining duration (in seconds) of all simulated DoS attacks on the double-spending protection service.
     * @return
     */
    public long getRemainingOfflineTimeSeconds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime waitUntil = offlineDSPRepository.getWaitUntil();
        if (waitUntil.isAfter(now)) {
            return Duration.between(now, waitUntil).toSeconds();
        } else {
            return 0;
        }
    }
}
