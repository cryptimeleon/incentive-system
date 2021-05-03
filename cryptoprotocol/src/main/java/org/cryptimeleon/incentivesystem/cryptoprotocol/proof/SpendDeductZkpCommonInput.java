package org.cryptimeleon.incentivesystem.cryptoprotocol.proof;

import lombok.AllArgsConstructor;
import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.SpendRequest;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Common Input for the spend-deduct ZKP
 */
@AllArgsConstructor
public class SpendDeductZkpCommonInput implements CommonInput {
    public final BigInteger k;
    public final Zn.ZnElement gamma;
    public final Zn.ZnElement c0;
    public final Zn.ZnElement c1;
    public final GroupElement dsid;
    public final GroupElement c0Pre;
    public final GroupElement c1Pre;
    public final GroupElement commitmentC0;
    public final GroupElementVector ctrace0;
    public final GroupElementVector ctrace1;

    /**
     * Alternative Constructor using a SpendRequest as input
     */
    public SpendDeductZkpCommonInput(SpendRequest spendRequest, BigInteger earnAmount, Zn.ZnElement gamma) {
        this.k = earnAmount;
        this.gamma = gamma;
        this.c0 = spendRequest.getC0();
        this.c1 = spendRequest.getC1();
        this.dsid = spendRequest.getDsid();
        this.c0Pre = spendRequest.getCPre0();
        this.c1Pre = spendRequest.getCPre1();
        this.commitmentC0 = spendRequest.getCommitmentC0();
        this.ctrace0 = spendRequest.getCTrace0();
        this.ctrace1 = spendRequest.getCTrace1();
    }
}
