package org.cryptimeleon.incentive.client.dto;

import java.util.Objects;
import java.util.UUID;

public class PutItemDto {
    UUID basketId;
    String itemId;
    int count;

    public PutItemDto(final UUID basketId, final String itemId, final int count) {
        this.basketId = basketId;
        this.itemId = itemId;
        this.count = count;
    }

    public UUID getBasketId() {
        return this.basketId;
    }

    public void setBasketId(final UUID basketId) {
        this.basketId = basketId;
    }

    public String getItemId() {
        return this.itemId;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PutItemDto)) return false;
        final PutItemDto other = (PutItemDto) o;
        if (!other.canEqual(this)) return false;
        if (this.getCount() != other.getCount()) return false;
        final Object this$basketId = this.getBasketId();
        final Object other$basketId = other.getBasketId();
        if (!Objects.equals(this$basketId, other$basketId)) return false;
        final Object this$itemId = this.getItemId();
        final Object other$itemId = other.getItemId();
        return Objects.equals(this$itemId, other$itemId);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PutItemDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getCount();
        final Object $basketId = this.getBasketId();
        result = result * PRIME + ($basketId == null ? 43 : $basketId.hashCode());
        final Object $itemId = this.getItemId();
        result = result * PRIME + ($itemId == null ? 43 : $itemId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "PutItemDto(basketId=" + this.getBasketId() + ", itemId=" + this.getItemId() + ", count=" + this.getCount() + ")";
    }
}
