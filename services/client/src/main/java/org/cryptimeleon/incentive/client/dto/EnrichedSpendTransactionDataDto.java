package org.cryptimeleon.incentive.client.dto;

import org.cryptimeleon.incentive.crypto.model.SpendTransactionData;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * A DTO for spend transaction data + some more data from the context of the transaction.
 */
public class EnrichedSpendTransactionDataDto {
    private String serializedSpendTransactionData;
    private BigInteger promotionId;
    private UUID tokenUpdateId;
    private String serializedMetadata;
    private List<BigInteger> basketPoints;

    @SuppressWarnings("unused")
    public EnrichedSpendTransactionDataDto() {
    }

    public EnrichedSpendTransactionDataDto(SpendTransactionData spendTransactionData, BigInteger promotionId, UUID tokenUpdateId, String serializedMetadata, List<BigInteger> basketPoints) {
        this.serializedSpendTransactionData = (new JSONConverter()).serialize(spendTransactionData.getRepresentation());
        this.promotionId = promotionId;
        this.tokenUpdateId = tokenUpdateId;
        this.serializedMetadata = serializedMetadata;
        this.basketPoints = basketPoints;
    }

    public String getSerializedSpendTransactionData() {
        return serializedSpendTransactionData;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public UUID getTokenUpdateId() {
        return tokenUpdateId;
    }

    public String getSerializedMetadata() {
        return serializedMetadata;
    }

    public List<BigInteger> getBasketPoints() {
        return basketPoints;
    }
}
