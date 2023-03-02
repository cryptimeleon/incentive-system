package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.incentive.crypto.model.SpendStoreRequest;
import org.cryptimeleon.incentive.crypto.model.SpendProviderRequest;
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

    public SpendDeductZkpCommonInput(SpendStoreRequest spendStoreRequest, Zn.ZnElement gamma) {
        this.gamma = gamma;
        this.c = spendStoreRequest.getC();
        this.dsid = spendStoreRequest.getDsid();
        this.c0Pre = spendStoreRequest.getCPre0();
        this.c1Pre = spendStoreRequest.getCPre1();
        this.commitmentC0 = spendStoreRequest.getC0();
    }

    public SpendDeductZkpCommonInput(SpendProviderRequest spendProviderRequest, Zn.ZnElement gamma) {
        this.gamma = gamma;
        this.c = spendProviderRequest.getC();
        this.dsid = spendProviderRequest.getDoubleSpendingId();
        this.c0Pre = spendProviderRequest.getcPre0();
        this.c1Pre = spendProviderRequest.getcPre1();
        this.commitmentC0 = spendProviderRequest.getC0();
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
