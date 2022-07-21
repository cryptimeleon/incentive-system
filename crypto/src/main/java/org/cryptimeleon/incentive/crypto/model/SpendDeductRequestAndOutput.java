package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Spend request and output of spend-deduct protocol bundled together in one class.
 * Needed to shorten double-spending protection service integration test.
 */
@Value
@AllArgsConstructor
public class SpendDeductRequestAndOutput {
    SpendRequest spendRequest;
    SpendDeductOutput spendDeductOutput;
}
