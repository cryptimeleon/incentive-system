package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

@Value
@AllArgsConstructor
public class TraceOutput {
    GroupElement dsidPrime;
    ZnElement dsTracePrime;
}
