package org.cryptimeleon.incentive.client.dto.inc;

import lombok.Value;

import java.util.List;

@Value
public class BulkRequestDto {
    List<EarnRequestDto> earnRequestDtoList;
    List<SpendRequestDto> spendRequestDtoList;
}
