package org.cryptimeleon.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasketDto {
    private UUID basketID;
    private List<BasketItemDto> basketItems;
    private List<String> rewardItems;
    private boolean paid;
    private boolean redeemed;
    private String redeemRequest;
    private long value;
}
