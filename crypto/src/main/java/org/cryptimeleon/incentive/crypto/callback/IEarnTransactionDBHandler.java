package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.EarnTransactionData;

public interface IEarnTransactionDBHandler {
    void addEarnData(EarnTransactionData earnTransactionData);
}
