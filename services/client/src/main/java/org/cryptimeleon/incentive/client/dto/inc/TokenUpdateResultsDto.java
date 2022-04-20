package org.cryptimeleon.incentive.client.dto.inc;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;

@Value
@AllArgsConstructor
@NoArgsConstructor
public class TokenUpdateResultsDto {
    @NonFinal
    List<ZkpTokenUpdateResultDto> zkpTokenUpdateResultDtoList;
    @NonFinal
    List<EarnTokenUpdateResultDto> earnTokenUpdateResultDtoList;
}
