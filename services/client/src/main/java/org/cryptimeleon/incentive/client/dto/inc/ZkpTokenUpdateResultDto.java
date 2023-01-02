package org.cryptimeleon.incentive.client.dto.inc;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

public final class ZkpTokenUpdateResultDto implements TokenUpdateResult {
    private final BigInteger promotionId;
    private final UUID tokenUpdateId;
    private final String serializedResponse;

    public ZkpTokenUpdateResultDto(final BigInteger promotionId, final UUID tokenUpdateId, final String serializedResponse) {
        this.promotionId = promotionId;
        this.tokenUpdateId = tokenUpdateId;
        this.serializedResponse = serializedResponse;
    }

    public BigInteger getPromotionId() {
        return this.promotionId;
    }

    public UUID getTokenUpdateId() {
        return this.tokenUpdateId;
    }

    public String getSerializedResponse() {
        return this.serializedResponse;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ZkpTokenUpdateResultDto)) return false;
        final ZkpTokenUpdateResultDto other = (ZkpTokenUpdateResultDto) o;
        final Object this$promotionId = this.getPromotionId();
        final Object other$promotionId = other.getPromotionId();
        if (!Objects.equals(this$promotionId, other$promotionId))
            return false;
        final Object this$tokenUpdateId = this.getTokenUpdateId();
        final Object other$tokenUpdateId = other.getTokenUpdateId();
        if (!Objects.equals(this$tokenUpdateId, other$tokenUpdateId))
            return false;
        final Object this$serializedResponse = this.getSerializedResponse();
        final Object other$serializedResponse = other.getSerializedResponse();
        return Objects.equals(this$serializedResponse, other$serializedResponse);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $promotionId = this.getPromotionId();
        result = result * PRIME + ($promotionId == null ? 43 : $promotionId.hashCode());
        final Object $tokenUpdateId = this.getTokenUpdateId();
        result = result * PRIME + ($tokenUpdateId == null ? 43 : $tokenUpdateId.hashCode());
        final Object $serializedResponse = this.getSerializedResponse();
        result = result * PRIME + ($serializedResponse == null ? 43 : $serializedResponse.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ZkpTokenUpdateResultDto(promotionId=" + this.getPromotionId() + ", tokenUpdateId=" + this.getTokenUpdateId() + ", serializedResponse=" + this.getSerializedResponse() + ")";
    }
}
