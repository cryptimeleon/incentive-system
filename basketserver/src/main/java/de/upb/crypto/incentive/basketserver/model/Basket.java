package de.upb.crypto.incentive.basketserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Basket {
    private UUID basketID;
    private Map<UUID, Integer> items;
    private boolean paid;
    private boolean redeemed;
    private String redeemRequest;
    // value must be set manually for serialization
    private int value;

    public Basket(UUID id) {
        basketID = id;
        items = new HashMap<>();
        paid = false;
        redeemed = false;
        redeemRequest = "";
    }
}
