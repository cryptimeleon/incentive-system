package org.cryptimeleon.incentive.services.incentive.repository;

import lombok.Value;
import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Data class modelling a single transaction that needs to be synced into the double-spending protection database.
 */
@Value
class DbSyncTask {
    BigInteger promotionId; // ID of the promotion that the transaction exploits
    Zn.ZnElement tid; // ID of the transaction
    SpendRequest spendRequest; // object describing the spend request that lead to the transaction
    DeductOutput deductOutput; // output (= spend response + double-spending tag) that the provider generated when processing the above spend request
}
