package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Common Input for the spend-deduct ZKP
 */
public class SpendDeductZkpCommonInput implements CommonInput {
    public final Zn.ZnElement gamma;
    public final Zn.ZnElement c;
    public final Zn.ZnElement dsid;
    public final GroupElement c0Pre;
    public final GroupElement c1Pre;
    public final GroupElement commitmentC0;

    /**
     * Alternative Constructor using a SpendRequest as input
     */
    public SpendDeductZkpCommonInput(SpendRequest spendRequest, Zn.ZnElement gamma) {
        this.gamma = gamma;
        this.c = spendRequest.getC();
        this.dsid = spendRequest.getDsid();
        this.c0Pre = spendRequest.getCPre0();
        this.c1Pre = spendRequest.getCPre1();
        this.commitmentC0 = spendRequest.getCommitmentC0();
    }

    public SpendDeductZkpCommonInput(Zn.ZnElement gamma, Zn.ZnElement c, Zn.ZnElement dsid, GroupElement c0Pre, GroupElement c1Pre, GroupElement commitmentC0) {
        this.gamma = gamma;
        this.c = c;
        this.dsid = dsid;
        this.c0Pre = c0Pre;
        this.c1Pre = c1Pre;
        this.commitmentC0 = commitmentC0;
    }
}
