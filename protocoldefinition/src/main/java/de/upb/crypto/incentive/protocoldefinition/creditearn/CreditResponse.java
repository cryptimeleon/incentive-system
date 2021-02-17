package de.upb.crypto.incentive.protocoldefinition.creditearn;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponse {
  private UUID id;
  private String serializedCreditResponse;
}
