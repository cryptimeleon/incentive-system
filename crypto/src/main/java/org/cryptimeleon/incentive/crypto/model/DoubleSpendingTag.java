package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data associated to a spend operation which the provider requires to trace double-spending.
 * Called 'dstag' in the cryptimeleon incentive system paper.
 */
@Value
@AllArgsConstructor
public class DoubleSpendingTag {
    Zn.ZnElement c0; // challenge for deriving the user secret key
    Zn.ZnElement c1; // challenge for deriving the encryption secret key
    Zn.ZnElement gamma; // challenge generation helper value
    Zn.ZnElement eskStarProv; // provider share for ElGamal encryption secret key
    GroupElementVector ctrace0;
    GroupElementVector ctrace1;
}
