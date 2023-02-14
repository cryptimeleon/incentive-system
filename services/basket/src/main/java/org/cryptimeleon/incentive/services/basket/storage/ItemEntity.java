package org.cryptimeleon.incentive.services.basket.storage;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "items")
public class ItemEntity {
    private String id;
    private String title;
    private Long price;
    @OneToMany
    private Set<ItemInBasketEntity> basketItems = new HashSet<>();

    @SuppressWarnings("unused")
    public ItemEntity() {
    }

    @SuppressWarnings("unused")
    public ItemEntity(String id, String title, Long price, Set<ItemInBasketEntity> basketItems) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.basketItems = basketItems;
    }

    @SuppressWarnings("unused")


    public String getTitle() {
        return title;
    }

    @SuppressWarnings("unused")
    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPrice() {
        return price;
    }

    @SuppressWarnings("unused")
    public void setPrice(Long price) {
        this.price = price;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemEntity that = (ItemEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, price);
    }
}
