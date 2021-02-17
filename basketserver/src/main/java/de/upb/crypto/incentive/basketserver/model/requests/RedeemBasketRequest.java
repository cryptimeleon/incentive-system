package de.upb.crypto.incentive.basketserver.model.requests;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dataclass for redeem request body. */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RedeemBasketRequest {
  UUID basketId;
  String redeemRequest;
  long value;
}
