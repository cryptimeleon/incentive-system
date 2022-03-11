package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Tuple for the private and public output of the deduct algorithm executed by the provider when processing an spend request.
 */
@Value
@AllArgsConstructor
public class DeductOutput {
    SpendResponse spendResponse;
    DoubleSpendingTag dstag;
}
