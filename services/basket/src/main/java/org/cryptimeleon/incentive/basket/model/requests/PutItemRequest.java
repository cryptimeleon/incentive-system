package org.cryptimeleon.incentive.basket.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Dataclass for put item request body.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PutItemRequest {
    UUID basketId;
    UUID itemId;
    int count;
}
