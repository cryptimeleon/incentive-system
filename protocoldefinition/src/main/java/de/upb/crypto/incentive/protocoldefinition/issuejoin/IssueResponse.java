package de.upb.crypto.incentive.protocoldefinition.issuejoin;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
  private UUID id;
  private String serializedIssueResponse;
}
