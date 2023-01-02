package org.cryptimeleon.incentive.client.dto;

import java.util.Objects;

/**
 * An item represents an item that can be added to a basket.
 * Hence, it does not have a count field as in the BasketItemDto.
 */
public class ItemDto {
    String id;
    String title;
    int price;

    public ItemDto(final String id, final String title, final int price) {
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

    public int getPrice() {
        return this.price;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ItemDto)) return false;
        final ItemDto other = (ItemDto) o;
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
        return other instanceof ItemDto;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPrice();
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ItemDto(id=" + this.getId() + ", title=" + this.getTitle() + ", price=" + this.getPrice() + ")";
    }
}
