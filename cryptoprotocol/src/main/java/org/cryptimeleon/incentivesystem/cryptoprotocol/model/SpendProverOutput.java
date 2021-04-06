package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class SpendProverOutput {
    SpendResponse spendResponse;
    DoubleSpendingTag dstag;
}
