package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.incentive.crypto.IssueJoinRandomness;

import java.util.Objects;

/**
 * Wrapper class for the output the user generates from the first step of the Join algorithm
 * (which is part of the Issue-Join protocol).
 * Consists of the random Zn elements that the user draws
 * as well as the join request it sends to the provider as input for its Issue algorithm.
 */
public class JoinFirstStepOutput {
    private final IssueJoinRandomness issueJoinRandomness;
    private final JoinRequest joinRequest;

    private JoinFirstStepOutput(IssueJoinRandomness issueJoinRandomness, JoinRequest joinRequest) {
        this.issueJoinRandomness = issueJoinRandomness;
        this.joinRequest = joinRequest;
    }

    public static JoinFirstStepOutput of(IssueJoinRandomness issueJoinRandomness, JoinRequest joinRequest) {
        return new JoinFirstStepOutput(issueJoinRandomness, joinRequest);
    }

    public IssueJoinRandomness getIssueJoinRandomness() {
        return this.issueJoinRandomness;
    }

    public JoinRequest getJoinRequest() {
        return this.joinRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoinFirstStepOutput that = (JoinFirstStepOutput) o;
        return Objects.equals(issueJoinRandomness, that.issueJoinRandomness) && Objects.equals(joinRequest, that.joinRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issueJoinRandomness, joinRequest);
    }

    public String toString() {
        return "JoinFirstStepOutput(issueJoinRandomness=" + this.getIssueJoinRandomness() + ", joinRequest=" + this.getJoinRequest() + ")";
    }
}
