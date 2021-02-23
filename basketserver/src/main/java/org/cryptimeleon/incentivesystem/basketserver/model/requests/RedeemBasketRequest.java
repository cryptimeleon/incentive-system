package org.cryptimeleon.incentivesystem.basketserver.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Dataclass for redeem request body.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RedeemBasketRequest {
    UUID basketId;
    String redeemRequest;
    long value;
}
