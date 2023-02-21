package org.cryptimeleon.incentive.client.dto.store;

import java.math.BigInteger;
import java.util.Objects;

public class EarnRequestStoreDto {
    private BigInteger promotionId;
    private String serializedRequest;

    @SuppressWarnings("unused")
    public EarnRequestStoreDto() {
    }

    public EarnRequestStoreDto(BigInteger promotionId, String serializedRequest) {
        this.promotionId = promotionId;
        this.serializedRequest = serializedRequest;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public String getSerializedRequest() {
        return serializedRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarnRequestStoreDto that = (EarnRequestStoreDto) o;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(serializedRequest, that.serializedRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, serializedRequest);
    }
}
