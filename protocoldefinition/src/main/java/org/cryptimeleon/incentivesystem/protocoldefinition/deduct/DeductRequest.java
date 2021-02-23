package org.cryptimeleon.incentivesystem.protocoldefinition.deduct;

import org.cryptimeleon.incentivesystem.protocoldefinition.model.Token;
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
