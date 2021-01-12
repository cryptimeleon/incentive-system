package de.upb.crypto.incentive.protocoldefinition.issuejoin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private UUID id;
    private String serializedIssueResponse;
}