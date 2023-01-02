package org.cryptimeleon.incentive.client.dto;

import java.util.Objects;

/**
 * A basket item represents an item that is in a basket and hence has a count field.
 */
public class BasketItemDto {
    String id;
    String title;
    int price;
    int count;

    public BasketItemDto() {
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

    public int getPrice() {
        return this.price;
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
        if (!(o instanceof BasketItemDto)) return false;
        final BasketItemDto other = (BasketItemDto) o;
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
        return other instanceof BasketItemDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPrice();
        result = result * PRIME + this.getCount();
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "BasketItemDto(id=" + this.getId() + ", title=" + this.getTitle() + ", price=" + this.getPrice() + ", count=" + this.getCount() + ")";
    }
}
