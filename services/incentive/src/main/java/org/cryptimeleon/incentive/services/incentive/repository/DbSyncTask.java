package org.cryptimeleon.incentive.services.incentive.repository;

import lombok.Value;
import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

@Value
class DbSyncTask {
    BigInteger promotionId;
    Zn.ZnElement tid;
    SpendRequest spendRequest;
    DeductOutput deductOutput;
}
