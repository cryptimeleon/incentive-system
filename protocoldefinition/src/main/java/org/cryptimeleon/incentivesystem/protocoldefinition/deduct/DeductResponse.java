package org.cryptimeleon.incentivesystem.protocoldefinition.deduct;

import org.cryptimeleon.incentivesystem.protocoldefinition.model.Token;
import org.cryptimeleon.incentivesystem.protocoldefinition.model.Reward;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductResponse {
    private Token token;
    private Reward reward;
}
