package org.cryptimeleon.incentive.services.issue.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestDTO {
    private String serializedUserPublicKey;
    private String serializedJoinRequest;
}
