package org.cryptimeleon.incentive.services.basket.api;

import io.swagger.annotations.ApiModelProperty;
import org.cryptimeleon.incentive.services.basket.storage.ItemEntity;

import java.util.Objects;

/**
 * Dataclass representing items that be purchased in a basket.
 */
public class Item {
    @ApiModelProperty("${item.id}")
    String id;
    @ApiModelProperty("${item.title}")
    final
    String title;
    @ApiModelProperty("${item.price}")
    final
    long price;

    public Item(ItemEntity itemEntity) {
        this.id = itemEntity.getId();
        this.title = itemEntity.getTitle();
        this.price = itemEntity.getPrice();
    }

    public Item(final String id, final String title, final long price) {
        this.id = id;
        this.title = title;
        this.price = price;
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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Item)) return false;
        final Item other = (Item) o;
        if (!other.canEqual(this)) return false;
        if (this.getPrice() != other.getPrice()) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (!Objects.equals(this$id, other$id)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        return Objects.equals(this$title, other$title);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Item;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $price = this.getPrice();
        result = result * PRIME + (int) ($price >>> 32 ^ $price);
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Item(id=" + this.getId() + ", title=" + this.getTitle() + ", price=" + this.getPrice() + ")";
    }
}
