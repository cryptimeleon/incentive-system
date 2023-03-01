package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.callback.ITransactionDBHandler;
import org.cryptimeleon.incentive.crypto.model.EarnProviderRequest;
import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Store all transaction for clearing and offline double-spending protection
 */
@Repository
public class TransactionRepository implements ITransactionDBHandler {
    // Some data could appear twice bc. users can re-do earn without gaining an advantage. Filter by the hash h / ecdsa signature
    private final List<EarnClearingData> earnTransactionDataList = new ArrayList<>();
    private final List<SpendTransactionData> spendTransactionDataList = new ArrayList<>();

    @Override
    public void addEarnData(EarnProviderRequest earnProviderRequest, byte[] h) {
        var dataToInsert = new EarnClearingData(h, earnProviderRequest);
        earnTransactionDataList.add(dataToInsert);
    }

    @Override
    public void addSpendData(SpendTransactionData spendTransactionData) {
        spendTransactionDataList.add(spendTransactionData);
    }


    static class EarnClearingData {

        private final byte[] h;
        private final EarnProviderRequest earnProviderRequest;

        public EarnClearingData(byte[] h, EarnProviderRequest earnProviderRequest) {
            this.h = h;
            this.earnProviderRequest = earnProviderRequest;

        }

        public byte[] getH() {
            return h;
        }

        public EarnProviderRequest getEarnRequestECDSA() {
            return earnProviderRequest;
        }
    }
}
