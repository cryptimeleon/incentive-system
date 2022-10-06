package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.cryptimeleon.incentive.crypto.IssueJoinRandomness;

/**
 * Wrapper class for the output the user generates from the first step of the Join algorithm
 * (which is part of the Issue-Join protocol).
 * Consists of the random Zn elements that the user draws
 * as well as the join request it sends to the provider as input for its Issue algorithm.
 */
@Value
@AllArgsConstructor(staticName = "of")
public class JoinFirstStepOutput {
    IssueJoinRandomness issueJoinRandomness;
    JoinRequest joinRequest;
}
