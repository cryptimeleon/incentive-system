package org.cryptimeleon.incentivesystem.cryptoprotocol.proof;

import lombok.AllArgsConstructor;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.math.structures.groups.GroupElement;

@AllArgsConstructor
public class SpendDeductCommonInput implements CommonInput {
    public final GroupElement dsid;
    public final GroupElement w;
}
