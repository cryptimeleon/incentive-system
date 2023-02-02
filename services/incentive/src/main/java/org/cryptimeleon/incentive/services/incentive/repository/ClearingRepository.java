package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.model.EarnRequestECDSA;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ClearingRepository {
    // Some data could appear twice bc. users can re-do earn without gaining an advantage. Filter by the hash h / ecdsa signature
    private final List<EarnClearingData> earnClearingData = new ArrayList<>();

    public void addEarnClearingData(EarnRequestECDSA earnRequestECDSA, byte[] h) {
        var dataToInsert = new EarnClearingData(h, earnRequestECDSA);
        earnClearingData.add(dataToInsert);
    }

    public List<EarnClearingData> getEarnClearingData() {
        return earnClearingData;
    }


    static class EarnClearingData {

        private final byte[] h;
        private final EarnRequestECDSA earnRequestECDSA;
        public EarnClearingData(byte[] h, EarnRequestECDSA earnRequestECDSA) {
            this.h = h;
            this.earnRequestECDSA = earnRequestECDSA;

        }

        public byte[] getH() {
            return h;
        }

        public EarnRequestECDSA getEarnRequestECDSA() {
            return earnRequestECDSA;
        }
    }
}
