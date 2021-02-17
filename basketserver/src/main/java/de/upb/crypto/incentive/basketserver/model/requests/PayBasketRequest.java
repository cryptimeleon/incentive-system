package de.upb.crypto.incentive.basketserver.model.requests;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dataclass for pay basket request body. */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PayBasketRequest {
  UUID basketId;
  long value;
}
