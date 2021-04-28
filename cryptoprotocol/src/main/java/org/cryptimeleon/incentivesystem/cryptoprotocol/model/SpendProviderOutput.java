package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Tuple for the private and public output of the provider when processing an spend-deduct request.
 */
@Value
@AllArgsConstructor
public class SpendProviderOutput {
    SpendResponse spendResponse;
    DoubleSpendingTag dstag;
}
