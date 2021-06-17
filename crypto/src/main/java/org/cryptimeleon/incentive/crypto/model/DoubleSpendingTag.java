package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data required for a provider to trace double-spending.
 * Called `dstag` in the T2 paper.
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
