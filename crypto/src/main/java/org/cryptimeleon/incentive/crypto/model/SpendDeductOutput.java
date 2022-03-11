package org.cryptimeleon.incentive.crypto.model;

import lombok.Value;

/**
 * Needed for testing the double-spending protection service.
 * Capsulates the resulting token and transaction from spend transaction.
 */
@Value
public class SpendDeductOutput {
    Token resultToken; // the token resulting from the spend-deduct protocol
    Transaction occuredTransaction; // the transaction object capsulating the data of the occurred transaction
}
