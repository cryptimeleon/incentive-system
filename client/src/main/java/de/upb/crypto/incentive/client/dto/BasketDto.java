package de.upb.crypto.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasketDto {
    private UUID basketID;
    private Map<UUID, Integer> items;
    private boolean paid;
    private boolean redeemed;
    private String redeemRequest;
    private long value;
}
