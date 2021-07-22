package org.cryptimeleon.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PutItemDto {
    UUID basketId;
    String itemId;
    int count;
}
