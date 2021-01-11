package de.upb.crypto.incentive.protocolmock.deduct;

import de.upb.crypto.incentive.protocolmock.model.Reward;
import de.upb.crypto.incentive.protocolmock.model.Token;
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
