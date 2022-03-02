package org.cryptimeleon.incentive.client.dto.inc;

import lombok.Value;

import java.math.BigInteger;

@Value
public class EarnTokenUpdateResultDto implements TokenUpdateResult {
    BigInteger promotionId;
    String serializedEarnResponse;
}
