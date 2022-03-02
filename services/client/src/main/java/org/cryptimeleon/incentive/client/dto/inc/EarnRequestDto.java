package org.cryptimeleon.incentive.client.dto.inc;

import lombok.Value;

import java.math.BigInteger;

@Value
public class EarnRequestDto {
    BigInteger promotionId;
    String serializedEarnRequest;
}
