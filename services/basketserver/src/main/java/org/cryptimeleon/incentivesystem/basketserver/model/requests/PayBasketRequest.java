package org.cryptimeleon.incentivesystem.basketserver.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Dataclass for pay basket request body.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PayBasketRequest {
    UUID basketId;
    long value;
}
