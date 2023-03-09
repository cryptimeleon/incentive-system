package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.callback.IEarnTransactionDBHandler;
import org.cryptimeleon.incentive.crypto.callback.ISpendTransactionDBHandler;
import org.cryptimeleon.incentive.crypto.model.EarnTransactionData;
import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Store all transaction for clearing and offline double-spending protection
 */
@Repository
public class TransactionRepository implements IEarnTransactionDBHandler, ISpendTransactionDBHandler {
    // Some data could appear twice bc. users can re-do earn without gaining an advantage. Filter by the hash h / ecdsa signature
    private final List<EarnTransactionData> earnTransactionDataList = new ArrayList<>();
    private final List<SpendTransactionData> spendTransactionDataList = new ArrayList<>();

    @Override
    public void addEarnData(EarnTransactionData earnTransactionData) {
        earnTransactionDataList.add(earnTransactionData);
    }

    @Override
    public void addSpendData(SpendTransactionData spendTransactionData) {
        spendTransactionDataList.add(spendTransactionData);
    }
}
