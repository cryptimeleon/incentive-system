package org.cryptimeleon.incentive.client.dto.inc;

import java.math.BigInteger;
import java.util.Objects;

public final class EarnRequestDto {
    private BigInteger promotionId;
    private String serializedEarnRequest;

    @SuppressWarnings("unused")
    public EarnRequestDto() {
    }

    public EarnRequestDto(final BigInteger promotionId, final String serializedEarnRequest) {
        this.promotionId = promotionId;
        this.serializedEarnRequest = serializedEarnRequest;
    }

    public BigInteger getPromotionId() {
        return this.promotionId;
    }

    public String getSerializedEarnRequest() {
        return this.serializedEarnRequest;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof EarnRequestDto)) return false;
        final EarnRequestDto other = (EarnRequestDto) o;
        final Object this$promotionId = this.getPromotionId();
        final Object other$promotionId = other.getPromotionId();
        if (!Objects.equals(this$promotionId, other$promotionId))
            return false;
        final Object this$serializedEarnRequest = this.getSerializedEarnRequest();
        final Object other$serializedEarnRequest = other.getSerializedEarnRequest();
        return Objects.equals(this$serializedEarnRequest, other$serializedEarnRequest);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $promotionId = this.getPromotionId();
        result = result * PRIME + ($promotionId == null ? 43 : $promotionId.hashCode());
        final Object $serializedEarnRequest = this.getSerializedEarnRequest();
        result = result * PRIME + ($serializedEarnRequest == null ? 43 : $serializedEarnRequest.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "EarnRequestDto(promotionId=" + this.getPromotionId() + ", serializedEarnRequest=" + this.getSerializedEarnRequest() + ")";
    }
}
