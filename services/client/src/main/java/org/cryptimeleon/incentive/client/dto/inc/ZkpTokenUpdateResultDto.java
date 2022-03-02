package org.cryptimeleon.incentive.client.dto.inc;

import lombok.Value;

import java.math.BigInteger;
import java.util.UUID;


@Value
public class ZkpTokenUpdateResultDto implements TokenUpdateResult{
    BigInteger promotionId;
    UUID tokenUpdateId;
    String serializedZkpUpdateResponse;
}

