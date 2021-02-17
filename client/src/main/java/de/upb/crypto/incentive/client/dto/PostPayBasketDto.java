package de.upb.crypto.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPayBasketDto {
    private UUID basketId;
    private long value;
}
