package org.cryptimeleon.incentive.client.dto.inc;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;

@Value
@AllArgsConstructor
public class TokenUpdateResultsDto {
    @NonFinal
    List<ZkpTokenUpdateResultDto> zkpTokenUpdateResultDtoList;
    @NonFinal
    List<EarnTokenUpdateResultDto> earnTokenUpdateResultDtoList;

    public TokenUpdateResultsDto() {
    }
}
