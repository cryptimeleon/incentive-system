package de.upb.crypto.incentive.services.credit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Model class representing a basket.
 * Copied from Basket Server, add to extra package?
 */
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
