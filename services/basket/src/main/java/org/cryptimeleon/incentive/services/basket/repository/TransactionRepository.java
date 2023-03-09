package org.cryptimeleon.incentive.services.basket.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * Store all transaction data for clearing and offline double-spending detection.
 * TODO: Send them to provider
 */
@Repository
public class TransactionRepository {
    final ArrayList<BasketEarnTransactionData> earnData = new ArrayList<>();
    final ArrayList<BasketSpendTransactionData> spendData = new ArrayList<>();

    public void addEarnData(BasketEarnTransactionData basketEarnTransactionData) {
        earnData.add(basketEarnTransactionData);
    }

    public void addSpendData(BasketSpendTransactionData basketSpendTransactionData) {
        spendData.add(basketSpendTransactionData);
    }

    public Optional<BasketSpendTransactionData> getSpendDataForBasketId(UUID basketId) {
        return spendData.stream()
                .filter(spendData -> spendData.getBasketId().equals(basketId))
                .findAny();
    }

    public Optional<BasketEarnTransactionData> getEarnDataForBasketId(UUID basketId) {
        return earnData.stream()
                .filter(earnData -> earnData.getBasketId().equals(basketId))
                .findAny();
    }
}

