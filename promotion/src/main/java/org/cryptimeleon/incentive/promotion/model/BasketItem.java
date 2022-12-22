package org.cryptimeleon.incentive.promotion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Data class that represents all items of a certain type that are in a basket.
 */
public final class BasketItem {
    private final String itemId;
    private final String title;
    private final int price;
    private final int count;

    /**
     * Old constructor. We do not use UUIDs for items anymore since they are too long for barcodes.
     */
    @Deprecated
    public BasketItem(UUID itemId, String title, int price, int count) {
        this.itemId = itemId.toString();
        this.title = title;
        this.price = price;
        this.count = count;
    }

    public BasketItem(String itemId, String title, int price, int count) {
        this.itemId = itemId;
        this.title = title;
        this.price = price;
        this.count = count;
    }

    public String getItemId() {
        return this.itemId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasketItem that = (BasketItem) o;
        return price == that.price && count == that.count && Objects.equals(itemId, that.itemId) && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, title, price, count);
    }

    public String toString() {
        return "BasketItem(itemId=" + this.getItemId() + ", title=" + this.getTitle() + ", price=" + this.getPrice() + ", count=" + this.getCount() + ")";
    }
}
