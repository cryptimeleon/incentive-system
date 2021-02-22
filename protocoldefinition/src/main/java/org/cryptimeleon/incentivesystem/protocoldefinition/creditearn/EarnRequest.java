package org.cryptimeleon.incentivesystem.protocoldefinition.creditearn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EarnRequest {
    private UUID id;
    private String serializedEarnRequest;
    private long earnAmount;
    private UUID basketId;
}
