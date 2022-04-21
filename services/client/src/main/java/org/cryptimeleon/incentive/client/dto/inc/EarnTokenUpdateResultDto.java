package org.cryptimeleon.incentive.client.dto.inc;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.math.BigInteger;

@Value
@AllArgsConstructor
@NoArgsConstructor
public class EarnTokenUpdateResultDto implements TokenUpdateResult {
    @NonFinal
    BigInteger promotionId;
    @NonFinal
    String serializedEarnResponse;
}
