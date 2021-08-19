package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Data class representing a spend transaction.
 * Note that the challenge generation helper value gamma is contained in the double spending tag
 * and thus not a field of the transaction class (DRY principle).
 */
@Value
public class Transaction {
    private Zn.ZnElement transactionID; // identifier for this transaction on protocol-level

    private BigInteger k; // number of points spent in this transaction ("spend amount")

    private DoubleSpendingTag dsTag; // data associated to a spend transaction that is required to trace double-spending, this contains challenge generation helper value gamma
}
