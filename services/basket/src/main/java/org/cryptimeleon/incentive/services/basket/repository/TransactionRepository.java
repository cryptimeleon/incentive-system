package org.cryptimeleon.incentive.services.basket.repository;

import org.cryptimeleon.incentive.crypto.callback.ITransactionDBHandler;
import org.cryptimeleon.incentive.crypto.model.EarnProviderRequest;
import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Store all transaction data for clearing and offline double-spending detection.
 * TODO: Send them to provider
 */
@Repository
public class TransactionRepository implements ITransactionDBHandler {
    final Map<EarnProviderRequest, byte[]> earnData = new HashMap<>();
    final ArrayList<SpendTransactionData> spendData = new ArrayList<>();

    @Override
    public void addEarnData(EarnProviderRequest earnProviderRequest, byte[] h) {
        earnData.put(earnProviderRequest, h);
    }

    @Override
    public void addSpendData(SpendTransactionData spendTransactionData) {
        spendData.add(spendTransactionData);
    }
}
