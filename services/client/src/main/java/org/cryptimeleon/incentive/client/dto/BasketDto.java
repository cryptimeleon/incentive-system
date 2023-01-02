package org.cryptimeleon.incentive.client.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BasketDto {
    private UUID basketID;
    private List<BasketItemDto> basketItems;
    private List<RewardItemDto> rewardItems;
    private boolean paid;
    private boolean redeemed;
    private String redeemRequest;
    private long value;

    @SuppressWarnings("unused")
    public BasketDto() {
    }

    public UUID getBasketID() {
        return this.basketID;
    }

    public List<BasketItemDto> getBasketItems() {
        return this.basketItems;
    }

    public List<RewardItemDto> getRewardItems() {
        return this.rewardItems;
    }

    public boolean isPaid() {
        return this.paid;
    }

    public boolean isRedeemed() {
        return this.redeemed;
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
        if (!(o instanceof BasketDto)) return false;
        final BasketDto other = (BasketDto) o;
        if (!other.canEqual(this)) return false;
        if (this.isPaid() != other.isPaid()) return false;
        if (this.isRedeemed() != other.isRedeemed()) return false;
        if (this.getValue() != other.getValue()) return false;
        final Object this$basketID = this.getBasketID();
        final Object other$basketID = other.getBasketID();
        if (!Objects.equals(this$basketID, other$basketID)) return false;
        final Object this$basketItems = this.getBasketItems();
        final Object other$basketItems = other.getBasketItems();
        if (!Objects.equals(this$basketItems, other$basketItems))
            return false;
        final Object this$rewardItems = this.getRewardItems();
        final Object other$rewardItems = other.getRewardItems();
        if (!Objects.equals(this$rewardItems, other$rewardItems))
            return false;
        final Object this$redeemRequest = this.getRedeemRequest();
        final Object other$redeemRequest = other.getRedeemRequest();
        return Objects.equals(this$redeemRequest, other$redeemRequest);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BasketDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isPaid() ? 79 : 97);
        result = result * PRIME + (this.isRedeemed() ? 79 : 97);
        final long $value = this.getValue();
        result = result * PRIME + (int) ($value >>> 32 ^ $value);
        final Object $basketID = this.getBasketID();
        result = result * PRIME + ($basketID == null ? 43 : $basketID.hashCode());
        final Object $basketItems = this.getBasketItems();
        result = result * PRIME + ($basketItems == null ? 43 : $basketItems.hashCode());
        final Object $rewardItems = this.getRewardItems();
        result = result * PRIME + ($rewardItems == null ? 43 : $rewardItems.hashCode());
        final Object $redeemRequest = this.getRedeemRequest();
        result = result * PRIME + ($redeemRequest == null ? 43 : $redeemRequest.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "BasketDto(basketID=" + this.getBasketID() + ", basketItems=" + this.getBasketItems() + ", rewardItems=" + this.getRewardItems() + ", paid=" + this.isPaid() + ", redeemed=" + this.isRedeemed() + ", redeemRequest=" + this.getRedeemRequest() + ", value=" + this.getValue() + ")";
    }
}
