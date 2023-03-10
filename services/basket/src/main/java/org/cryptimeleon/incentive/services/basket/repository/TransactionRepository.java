package org.cryptimeleon.incentive.services.basket.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
    private final ArrayList<BasketEarnTransactionData> earnData = new ArrayList<>();
    private final ArrayList<BasketSpendTransactionData> spendData = new ArrayList<>();

    private final ProviderTxRepository providerTxRepository;

    @Autowired
    public TransactionRepository(ProviderTxRepository providerTxRepository) {
        this.providerTxRepository = providerTxRepository;
    }


    public void addEarnData(BasketEarnTransactionData basketEarnTransactionData) {
        earnData.add(basketEarnTransactionData);
    }

    @Async
    public void addSpendData(BasketSpendTransactionData basketSpendTransactionData) {
        spendData.add(basketSpendTransactionData);
        providerTxRepository.sendBasketTransactioData(basketSpendTransactionData);
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

