package org.cryptimeleon.incentivesystem.crypto.benchmark;

/**
 * Enum for the current state of the benchmark (which operation is currently benchmarked)
 * This is required to pass the current state/progress to a consumer.
 */
public enum BenchmarkState {
    ISSUE_JOIN,
    CREDIT_EARN,
    SPEND_DEDUCT,
}
