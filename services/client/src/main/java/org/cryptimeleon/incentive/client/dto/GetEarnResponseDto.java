package org.cryptimeleon.incentive.client.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetEarnResponseDto {
    private UUID id;
    private String serializedCreditResponse;
}