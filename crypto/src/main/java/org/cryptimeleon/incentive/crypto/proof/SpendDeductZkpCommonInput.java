package org.cryptimeleon.incentive.crypto.proof;

import lombok.AllArgsConstructor;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Common Input for the spend-deduct ZKP
 */
@AllArgsConstructor
public class SpendDeductZkpCommonInput implements CommonInput {
    public final Zn.ZnElement gamma;
    public final Zn.ZnElement c0;
    public final Zn.ZnElement c1;
    public final GroupElement dsid;
    public final GroupElement c0Pre;
    public final GroupElement c1Pre;
    public final GroupElement commitmentC0;
    public final GroupElementVector ctrace0;
    public final GroupElementVector ctrace1;
    public final RingElementVector K;

    /**
     * Alternative Constructor using a SpendRequest as input
     */
    public SpendDeductZkpCommonInput(SpendRequest spendRequest, RingElementVector earnAmounts, Zn.ZnElement gamma) {
        this.gamma = gamma;
        this.c0 = spendRequest.getC0();
        this.c1 = spendRequest.getC1();
        this.dsid = spendRequest.getDsid();
        this.c0Pre = spendRequest.getCPre0();
        this.c1Pre = spendRequest.getCPre1();
        this.commitmentC0 = spendRequest.getCommitmentC0();
        this.ctrace0 = spendRequest.getCTrace0();
        this.ctrace1 = spendRequest.getCTrace1();
        this.K = earnAmounts;
    }
}
