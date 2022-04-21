package org.cryptimeleon.incentive.client.dto.inc;


import lombok.Value;

import java.math.BigInteger;
import java.util.UUID;

@Value
public class SpendRequestDto {
    BigInteger promotionId;
    UUID tokenUpdateId;
    String serializedSpendRequest;
    String serializedMetadata;
}
