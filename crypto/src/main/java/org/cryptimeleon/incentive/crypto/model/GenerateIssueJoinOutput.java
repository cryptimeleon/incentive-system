package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.cryptimeleon.incentive.crypto.IssueJoinRandomness;

@Value
@AllArgsConstructor(staticName = "of")
public class GenerateIssueJoinOutput {
    IssueJoinRandomness issueJoinRandomness;
    JoinRequest joinRequest;
}
