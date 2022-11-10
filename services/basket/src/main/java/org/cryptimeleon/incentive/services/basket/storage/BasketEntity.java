package org.cryptimeleon.incentive.services.basket.storage;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;
import java.util.UUID;

@Entity
public class BasketEntity {
    @Id
    private UUID basketID;
    @OneToMany
    private Set<ItemInBasketEntity> basketItems;
    @ElementCollection
    private Set<String> rewardItems;
    private boolean paid;
    private boolean redeemed;
    private boolean locked;
    private String redeemRequest;
    // private long value;
}
