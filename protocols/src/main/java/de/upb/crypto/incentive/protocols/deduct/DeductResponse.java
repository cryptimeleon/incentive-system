package de.upb.crypto.incentive.protocols.deduct;

import de.upb.crypto.incentive.protocols.model.Reward;
import de.upb.crypto.incentive.protocols.model.Token;
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
