package de.upb.crypto.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRedeemBasketDto {
    UUID basketId;
    String redeemRequest;
    long value;
}
