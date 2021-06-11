package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data associated to a spend operation which the provider requires to trace double-spending.
 * Called `dstag` in the cryptimeleon incentive system paper.
 */
@Value
@AllArgsConstructor
public class DoubleSpendingTag {
    Zn.ZnElement c0;
    Zn.ZnElement c1;
    Zn.ZnElement gamma;
    Zn.ZnElement eskStarProv;
    GroupElementVector ctrace0;
    GroupElementVector ctrace1;
}
