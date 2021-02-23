package org.cryptimeleon.incentivesystem.protocoldefinition.issuejoin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {
    // id is used to identify response as client in case there are several open requests
    private UUID id;
    private String serializedJoinRequest;
}
