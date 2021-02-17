package de.upb.crypto.incentive.basketserver.model.requests;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dataclass for put item request body. */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PutItemRequest {
  UUID basketId;
  UUID itemId;
  int count;
}
