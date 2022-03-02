package org.cryptimeleon.incentive.client.dto.inc;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.math.BigInteger;
import java.util.UUID;


@Value
@AllArgsConstructor
public class ZkpTokenUpdateResultDto implements TokenUpdateResult {
    @NonFinal
    BigInteger promotionId;
    @NonFinal
    UUID tokenUpdateId;
    @NonFinal
    String serializedZkpUpdateResponse;

    public ZkpTokenUpdateResultDto() {
    }
}

