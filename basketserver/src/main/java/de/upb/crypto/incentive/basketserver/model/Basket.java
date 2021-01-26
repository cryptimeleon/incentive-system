package de.upb.crypto.incentive.basketserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Basket {
    private UUID basketID;
    private Map<UUID, Integer> items;
    private boolean paid;
    private boolean redeemed;
    private String redeemRequest;
    // value is computed, only for serialization
    private int value;

    public Basket(UUID id) {
        basketID = id;
        items = new HashMap();
        paid = false;
        redeemed = false;
        redeemRequest = "";
    }
}
