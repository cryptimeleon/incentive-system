package org.cryptimeleon.incentive.client.dto.inc;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.math.BigInteger;

@Value
@AllArgsConstructor
public class EarnTokenUpdateResultDto implements TokenUpdateResult {
    @NonFinal
    BigInteger promotionId;
    @NonFinal
    String serializedEarnResponse;

    public EarnTokenUpdateResultDto() {
    }
}
