package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.EarnRequestECDSA;

public interface IClearingDBHandler {
    void addEarningDataToClearingDB(EarnRequestECDSA earnRequestECDSA, byte[] h);
}
