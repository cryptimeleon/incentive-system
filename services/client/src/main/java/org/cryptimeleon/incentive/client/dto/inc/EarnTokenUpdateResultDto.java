package org.cryptimeleon.incentive.client.dto.inc;

import java.math.BigInteger;
import java.util.Objects;

public final class EarnTokenUpdateResultDto implements TokenUpdateResult {
    private final BigInteger promotionId;
    private final String serializedEarnResponse;

    public EarnTokenUpdateResultDto(final BigInteger promotionId, final String serializedEarnResponse) {
        this.promotionId = promotionId;
        this.serializedEarnResponse = serializedEarnResponse;
    }

    public BigInteger getPromotionId() {
        return this.promotionId;
    }

    public String getSerializedEarnResponse() {
        return this.serializedEarnResponse;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof EarnTokenUpdateResultDto)) return false;
        final EarnTokenUpdateResultDto other = (EarnTokenUpdateResultDto) o;
        final Object this$promotionId = this.getPromotionId();
        final Object other$promotionId = other.getPromotionId();
        if (!Objects.equals(this$promotionId, other$promotionId))
            return false;
        final Object this$serializedEarnResponse = this.getSerializedEarnResponse();
        final Object other$serializedEarnResponse = other.getSerializedEarnResponse();
        return Objects.equals(this$serializedEarnResponse, other$serializedEarnResponse);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $promotionId = this.getPromotionId();
        result = result * PRIME + ($promotionId == null ? 43 : $promotionId.hashCode());
        final Object $serializedEarnResponse = this.getSerializedEarnResponse();
        result = result * PRIME + ($serializedEarnResponse == null ? 43 : $serializedEarnResponse.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "EarnTokenUpdateResultDto(promotionId=" + this.getPromotionId() + ", serializedEarnResponse=" + this.getSerializedEarnResponse() + ")";
    }
}
