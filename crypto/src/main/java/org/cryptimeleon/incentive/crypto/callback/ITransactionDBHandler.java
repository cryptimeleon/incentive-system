package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.EarnRequestECDSA;
import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;

/**
 * Interface to support a lambda function as a callback for adding clearing / doublespending protection data to some storage.
 */
public interface ITransactionDBHandler {
    /**
     * Add data from earn request required for clearing to some datasource.
     */
    void addEarnData(EarnRequestECDSA earnRequestECDSA, byte[] h);

    /**
     * Add data from spend request required for clearing and double-spending protection to some datasource.
     */
    void addSpendData(SpendTransactionData spendTransactionData);
}
