package org.cryptimeleon.incentive.services.basket.repository;

import org.cryptimeleon.incentive.crypto.callback.ITransactionDBHandler;
import org.cryptimeleon.incentive.crypto.model.EarnRequestECDSA;
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
    Map<EarnRequestECDSA, byte[]> earnData = new HashMap<>();
    ArrayList<SpendTransactionData> spendData = new ArrayList<>();

    @Override
    public void addEarnData(EarnRequestECDSA earnRequestECDSA, byte[] h) {
        earnData.put(earnRequestECDSA, h);
    }

    @Override
    public void addSpendData(SpendTransactionData spendTransactionData) {
        spendData.add(spendTransactionData);
    }
}
