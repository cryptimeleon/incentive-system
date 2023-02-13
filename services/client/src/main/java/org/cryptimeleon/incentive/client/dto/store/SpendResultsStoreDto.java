package org.cryptimeleon.incentive.client.dto.store;

import java.math.BigInteger;
import java.util.Objects;

public class SpendResultsStoreDto {
    private BigInteger promotionId;
    private String serializedSpendCouponSignature;

    @SuppressWarnings("unused")
    public SpendResultsStoreDto() {
    }

    public SpendResultsStoreDto(BigInteger promotionId, String serializedSpendCouponSignature) {
        this.promotionId = promotionId;
        this.serializedSpendCouponSignature = serializedSpendCouponSignature;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public String getSerializedSpendCouponSignature() {
        return serializedSpendCouponSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendResultsStoreDto that = (SpendResultsStoreDto) o;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(serializedSpendCouponSignature, that.serializedSpendCouponSignature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, serializedSpendCouponSignature);
    }
}
