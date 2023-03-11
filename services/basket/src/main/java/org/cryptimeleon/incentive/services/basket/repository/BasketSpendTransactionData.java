package org.cryptimeleon.incentive.services.basket.repository;

import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class BasketSpendTransactionData {
    private final UUID basketId;
    private final Promotion promotion;
    private final ZkpTokenUpdate tokenUpdate;
    private final SpendTransactionData spendTransactionData;
    private final String serializedMetadata;
    private final List<BigInteger> basketPoinst;

    public BasketSpendTransactionData(UUID basketId, Promotion promotion, ZkpTokenUpdate tokenUpdate, SpendTransactionData spendTransactionData, String serializedMetadata, List<BigInteger> basketPoints) {
        this.basketId = basketId;
        this.promotion = promotion;
        this.tokenUpdate = tokenUpdate;
        this.spendTransactionData = spendTransactionData;
        this.serializedMetadata = serializedMetadata;
        this.basketPoinst = basketPoints;
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

    public String getSerializedMetadata() {
        return serializedMetadata;
    }

    public List<BigInteger> getBasketPoinst() {
        return basketPoinst;
    }
}
