package org.cryptimeleon.incentivesystem.cryptoprotocol.proof;

import lombok.AllArgsConstructor;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

@AllArgsConstructor
public class SpendDeductCommonInput implements CommonInput {
    public final int eskDecVectorSize;
    public final BigInteger k;
    public final Zn.ZnElement gamma;
    public final Zn.ZnElement c0;
    public final Zn.ZnElement c1;
    public final GroupElement dsid;
    public final GroupElement w;
    public final GroupElement c0Pre;
    public final GroupElement c1Pre;
    public final GroupElement commitmentC0;
    public final GroupElement commitmentC1;
    public final Vector<GroupElement> ctrace0;
    public final Vector<GroupElement> ctrace1;
}
