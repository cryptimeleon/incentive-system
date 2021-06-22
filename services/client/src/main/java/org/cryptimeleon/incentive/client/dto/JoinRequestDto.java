package org.cryptimeleon.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestDto {
    private String serializedUserPublicKey;
    private String serializedJoinRequest;
}
