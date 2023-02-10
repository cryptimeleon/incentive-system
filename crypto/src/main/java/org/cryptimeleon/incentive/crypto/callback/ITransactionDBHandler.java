package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.EarnRequestECDSA;
import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;

/**
 * Interface to support a lambda function as a callback for adding clearing / doublespending protection data to some storage.
 */
public interface ITransactionDBHandler {
    void addEarnData(EarnRequestECDSA earnRequestECDSA, byte[] h);

    void addSpendData(SpendTransactionData spendTransactionData);
}
