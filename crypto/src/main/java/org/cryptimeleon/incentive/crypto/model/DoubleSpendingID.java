package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.math.BigInteger;

@Value
@AllArgsConstructor
public class DoubleSpendingID{
    GroupElement dsid;
    BigInteger promotionId;
}
