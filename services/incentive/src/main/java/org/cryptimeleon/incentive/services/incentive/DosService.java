package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.incentive.services.incentive.repository.ScheduledOfflineDSPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class DosService {

    private final ScheduledOfflineDSPRepository offlineDSPRepository;

    @Autowired
    public DosService(ScheduledOfflineDSPRepository offlineDSPRepository) {
        this.offlineDSPRepository = offlineDSPRepository;
    }

    public void addShortWaitPeriod() {
        offlineDSPRepository.addShortWaitPeriod();
    }

    public void addLongWaitPeriod() {
        offlineDSPRepository.addLongWaitPeriod();
    }

    public void removeAllWaitPeriod() {
        offlineDSPRepository.removeAllWaitPeriod();
    }

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
