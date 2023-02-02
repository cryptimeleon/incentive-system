package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.EarnRequestECDSA;

/**
 * Interface to support a lambda function as a callback for adding clearing data to some storage.
 */
public interface IClearingDBHandler {
    void addEarningDataToClearingDB(EarnRequestECDSA earnRequestECDSA, byte[] h);
}
