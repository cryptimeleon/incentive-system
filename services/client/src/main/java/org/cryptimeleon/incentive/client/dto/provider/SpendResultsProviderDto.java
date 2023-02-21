package org.cryptimeleon.incentive.client.dto.provider;

import java.math.BigInteger;

public class SpendResultsProviderDto {
    private BigInteger promotionId;
    private String serializedSpendResult;

    @SuppressWarnings("unused")
    public SpendResultsProviderDto() {
    }

    public SpendResultsProviderDto(BigInteger promotionId, String serializedSpendResult) {
        this.promotionId = promotionId;
        this.serializedSpendResult = serializedSpendResult;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public String getSerializedSpendResult() {
        return serializedSpendResult;
    }
}
