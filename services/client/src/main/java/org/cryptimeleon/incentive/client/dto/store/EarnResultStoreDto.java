package org.cryptimeleon.incentive.client.dto.store;

import java.math.BigInteger;
import java.util.Objects;

public class EarnResultStoreDto {
    private BigInteger promotionId;
    private String serializedEarnCouponSignature;

    @SuppressWarnings("unused")
    public EarnResultStoreDto() {
    }

    public EarnResultStoreDto(BigInteger promotionId, String serializedEarnCouponSignature) {
        this.promotionId = promotionId;
        this.serializedEarnCouponSignature = serializedEarnCouponSignature;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public String getSerializedEarnCouponSignature() {
        return serializedEarnCouponSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarnResultStoreDto that = (EarnResultStoreDto) o;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(serializedEarnCouponSignature, that.serializedEarnCouponSignature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, serializedEarnCouponSignature);
    }
}
