package org.cryptimeleon.incentive.client.dto.inc;

import lombok.Value;

import java.util.List;

@Value
public class TokenUpdateResultsDto {
    List<ZkpTokenUpdateResultDto> zkpTokenUpdateResultDtoList;
    List<EarnTokenUpdateResultDto> earnTokenUpdateResultDtoList;
}
