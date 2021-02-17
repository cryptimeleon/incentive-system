package de.upb.crypto.incentive.protocoldefinition.deduct;

import de.upb.crypto.incentive.protocoldefinition.model.Token;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductRequest {
  private Token token;
  private int rewardValue;
}
