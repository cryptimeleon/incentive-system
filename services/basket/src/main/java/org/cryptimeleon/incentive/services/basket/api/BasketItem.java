package org.cryptimeleon.incentive.services.basket.api;

import io.swagger.annotations.ApiModelProperty;
import org.cryptimeleon.incentive.services.basket.storage.ItemEntity;

import java.util.Objects;

public class BasketItem {
    @ApiModelProperty("${item.id}")
    String id;
    @ApiModelProperty("${item.title}")
    String title;
    @ApiModelProperty("${item.price}")
    long price;
    @ApiModelProperty("${item.count}")
    int count;

    @SuppressWarnings("unused")
    public BasketItem() {
    }

    public BasketItem(ItemEntity itemEntity, int count) {
        this.id = itemEntity.getId();
        this.title = itemEntity.getTitle();
        this.price = itemEntity.getPrice();
        this.count = count;
    }

    public BasketItem(Item item, int count) {
        this.id = item.getId();
        this.title = item.getTitle();
        this.price = item.getPrice();
        this.count = count;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public long getPrice() {
        return this.price;
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BasketItem)) return false;
        final BasketItem other = (BasketItem) o;
        if (!other.canEqual(this)) return false;
        if (this.getPrice() != other.getPrice()) return false;
        if (this.getCount() != other.getCount()) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (!Objects.equals(this$id, other$id)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        return Objects.equals(this$title, other$title);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BasketItem;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $price = this.getPrice();
        result = result * PRIME + (int) ($price >>> 32 ^ $price);
        result = result * PRIME + this.getCount();
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "BasketItem(id=" + this.getId() + ", title=" + this.getTitle() + ", price=" + this.getPrice() + ", count=" + this.getCount() + ")";
    }
}
