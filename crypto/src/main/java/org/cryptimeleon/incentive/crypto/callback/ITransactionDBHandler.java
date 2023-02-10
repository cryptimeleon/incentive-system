package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.EarnRequestECDSA;

/**
 * Interface to support a lambda function as a callback for adding clearing / doublespending protection data to some storage.
 */
public interface ITransactionDBHandler {
    void addEarningDataToClearingDB(EarnRequestECDSA earnRequestECDSA, byte[] h);
}
