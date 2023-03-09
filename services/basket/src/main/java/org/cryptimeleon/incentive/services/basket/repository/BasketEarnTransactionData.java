package org.cryptimeleon.incentive.services.basket.repository;

import org.cryptimeleon.incentive.crypto.model.EarnTransactionData;

import java.util.UUID;

public class BasketEarnTransactionData {
    private final UUID basketId;
    private final EarnTransactionData earnTransactionData;

    public BasketEarnTransactionData(UUID basketId, EarnTransactionData earnTransactionData) {
        this.basketId = basketId;
        this.earnTransactionData = earnTransactionData;
    }

    public UUID getBasketId() {
        return basketId;
    }

    public EarnTransactionData getEarnTransactionData() {
        return earnTransactionData;
    }
}
