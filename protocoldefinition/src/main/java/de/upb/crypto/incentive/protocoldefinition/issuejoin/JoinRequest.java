package de.upb.crypto.incentive.protocoldefinition.issuejoin;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {
  // id is used to identify response as client in case there are several open
  // requests
  private UUID id;
  private String serializedJoinRequest;
}
