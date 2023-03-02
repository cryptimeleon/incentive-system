package org.cryptimeleon.incentive.client.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BasketDto {
    private UUID basketID;
    private List<BasketItemDto> basketItems;
    private List<RewardItemDto> rewardItems;
    private boolean paid;
    private long value;

    @SuppressWarnings("unused")
    public BasketDto() {
    }

    @SuppressWarnings("unused")
    public BasketDto(UUID basketID, List<BasketItemDto> basketItems, List<RewardItemDto> rewardItems, boolean paid, long value) {
        this.basketID = basketID;
        this.basketItems = basketItems;
        this.rewardItems = rewardItems;
        this.paid = paid;
        this.value = value;
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

    public long getValue() {
        return this.value;
    }

    public void setValue(final long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasketDto basketDto = (BasketDto) o;
        return paid == basketDto.paid && value == basketDto.value && Objects.equals(basketID, basketDto.basketID) && Objects.equals(basketItems, basketDto.basketItems) && Objects.equals(rewardItems, basketDto.rewardItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basketID, basketItems, rewardItems, paid, value);
    }
}
