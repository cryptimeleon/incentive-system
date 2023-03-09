package org.cryptimeleon.incentive.services.basket.repository;

import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;

import java.util.UUID;

public class BasketSpendTransactionData {
    private final UUID basketId;
    private final Promotion promotion;
    private final ZkpTokenUpdate tokenUpdate;
    private final SpendTransactionData spendTransactionData;

    public BasketSpendTransactionData(UUID basketId, Promotion promotion, ZkpTokenUpdate tokenUpdate, SpendTransactionData spendTransactionData) {
        this.basketId = basketId;
        this.promotion = promotion;
        this.tokenUpdate = tokenUpdate;
        this.spendTransactionData = spendTransactionData;
    }

    public UUID getBasketId() {
        return basketId;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public ZkpTokenUpdate getTokenUpdate() {
        return tokenUpdate;
    }

    public SpendTransactionData getSpendTransactionData() {
        return spendTransactionData;
    }
}
