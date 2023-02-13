package org.cryptimeleon.incentive.client.dto.store;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

public class SpendRequestStoreDto {
    private String serializedRequest;
    private BigInteger promotionId;
    private UUID tokenUpdateId;
    private String serializedTokenUpdateMetadata;

    @SuppressWarnings("unused")
    public SpendRequestStoreDto() {
    }

    public SpendRequestStoreDto(String serializedRequest, BigInteger promotionId, UUID tokenUpdateId, String serializedTokenUpdateMetadata) {
        this.serializedRequest = serializedRequest;
        this.promotionId = promotionId;
        this.tokenUpdateId = tokenUpdateId;
        this.serializedTokenUpdateMetadata = serializedTokenUpdateMetadata;
    }

    public String getSerializedRequest() {
        return serializedRequest;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public UUID getTokenUpdateId() {
        return tokenUpdateId;
    }

    public String getSerializedTokenUpdateMetadata() {
        return serializedTokenUpdateMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendRequestStoreDto that = (SpendRequestStoreDto) o;
        return Objects.equals(serializedRequest, that.serializedRequest) && Objects.equals(promotionId, that.promotionId) && Objects.equals(tokenUpdateId, that.tokenUpdateId) && Objects.equals(serializedTokenUpdateMetadata, that.serializedTokenUpdateMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serializedRequest, promotionId, tokenUpdateId, serializedTokenUpdateMetadata);
    }
}
