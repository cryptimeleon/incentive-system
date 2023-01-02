package org.cryptimeleon.incentive.services.basket.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Dataclass for redeem request body.
 */
public class RedeemBasketRequest {
    UUID basketId;
    String redeemRequest;
    long value;

    public RedeemBasketRequest(final UUID basketId, final String redeemRequest, final long value) {
        this.basketId = basketId;
        this.redeemRequest = redeemRequest;
        this.value = value;
    }

    public UUID getBasketId() {
        return this.basketId;
    }

    public String getRedeemRequest() {
        return this.redeemRequest;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(final long value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RedeemBasketRequest)) return false;
        final RedeemBasketRequest other = (RedeemBasketRequest) o;
        if (!other.canEqual(this)) return false;
        if (this.getValue() != other.getValue()) return false;
        final Object this$basketId = this.getBasketId();
        final Object other$basketId = other.getBasketId();
        if (!Objects.equals(this$basketId, other$basketId)) return false;
        final Object this$redeemRequest = this.getRedeemRequest();
        final Object other$redeemRequest = other.getRedeemRequest();
        return Objects.equals(this$redeemRequest, other$redeemRequest);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RedeemBasketRequest;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $value = this.getValue();
        result = result * PRIME + (int) ($value >>> 32 ^ $value);
        final Object $basketId = this.getBasketId();
        result = result * PRIME + ($basketId == null ? 43 : $basketId.hashCode());
        final Object $redeemRequest = this.getRedeemRequest();
        result = result * PRIME + ($redeemRequest == null ? 43 : $redeemRequest.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "RedeemBasketRequest(basketId=" + this.getBasketId() + ", redeemRequest=" + this.getRedeemRequest() + ", value=" + this.getValue() + ")";
    }
}
