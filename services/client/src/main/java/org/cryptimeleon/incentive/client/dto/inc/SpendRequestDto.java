package org.cryptimeleon.incentive.client.dto.inc;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

public final class SpendRequestDto {
    private BigInteger promotionId;
    private UUID tokenUpdateId;
    private String serializedSpendRequest;
    private String serializedMetadata;

    @SuppressWarnings("unused")
    public SpendRequestDto() {
    }

    public SpendRequestDto(final BigInteger promotionId, final UUID tokenUpdateId, final String serializedSpendRequest, final String serializedMetadata) {
        this.promotionId = promotionId;
        this.tokenUpdateId = tokenUpdateId;
        this.serializedSpendRequest = serializedSpendRequest;
        this.serializedMetadata = serializedMetadata;
    }

    public BigInteger getPromotionId() {
        return this.promotionId;
    }

    public UUID getTokenUpdateId() {
        return this.tokenUpdateId;
    }

    public String getSerializedSpendRequest() {
        return this.serializedSpendRequest;
    }

    public String getSerializedMetadata() {
        return this.serializedMetadata;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SpendRequestDto)) return false;
        final SpendRequestDto other = (SpendRequestDto) o;
        final Object this$promotionId = this.getPromotionId();
        final Object other$promotionId = other.getPromotionId();
        if (!Objects.equals(this$promotionId, other$promotionId))
            return false;
        final Object this$tokenUpdateId = this.getTokenUpdateId();
        final Object other$tokenUpdateId = other.getTokenUpdateId();
        if (!Objects.equals(this$tokenUpdateId, other$tokenUpdateId))
            return false;
        final Object this$serializedSpendRequest = this.getSerializedSpendRequest();
        final Object other$serializedSpendRequest = other.getSerializedSpendRequest();
        if (!Objects.equals(this$serializedSpendRequest, other$serializedSpendRequest))
            return false;
        final Object this$serializedMetadata = this.getSerializedMetadata();
        final Object other$serializedMetadata = other.getSerializedMetadata();
        return Objects.equals(this$serializedMetadata, other$serializedMetadata);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $promotionId = this.getPromotionId();
        result = result * PRIME + ($promotionId == null ? 43 : $promotionId.hashCode());
        final Object $tokenUpdateId = this.getTokenUpdateId();
        result = result * PRIME + ($tokenUpdateId == null ? 43 : $tokenUpdateId.hashCode());
        final Object $serializedSpendRequest = this.getSerializedSpendRequest();
        result = result * PRIME + ($serializedSpendRequest == null ? 43 : $serializedSpendRequest.hashCode());
        final Object $serializedMetadata = this.getSerializedMetadata();
        result = result * PRIME + ($serializedMetadata == null ? 43 : $serializedMetadata.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "SpendRequestDto(promotionId=" + this.getPromotionId() + ", tokenUpdateId=" + this.getTokenUpdateId() + ", serializedSpendRequest=" + this.getSerializedSpendRequest() + ", serializedMetadata=" + this.getSerializedMetadata() + ")";
    }
}
