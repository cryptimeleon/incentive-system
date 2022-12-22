package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpCommonInput;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;

/**
 * Data class for the request sent by a user in spend-deduct.
 */
public class SpendRequest implements Representable {
    private final GroupElement dsid;

    private final FiatShamirProof spendDeductZkp;

    private final Zn.ZnElement c0;

    private final Zn.ZnElement c1;

    private final GroupElement cPre0;

    private final GroupElement cPre1;

    private final GroupElementVector cTrace0;

    private final GroupElementVector cTrace1;

    private final GroupElement commitmentC0; // do not send C_1 since it should be equal to g_1 anyways

    private final SPSEQSignature sigma;


    /**
     * A constructor for the spend request based on a representation and additional data required to retrieve the
     * original values.
     *
     * @param repr                  representation of the spend request
     * @param pp                    public parameters
     * @param fiatShamirProofSystem the fiat shamir proof system used to create the proof to be restored
     * @param tid                   the transaction id
     */
    public SpendRequest(Representation repr, IncentivePublicParameters pp, FiatShamirProofSystem fiatShamirProofSystem, Zn.ZnElement tid, UniqueByteRepresentable userChoice) {
        var listRepr = repr.list();
        var zn = pp.getBg().getZn();
        var groupG1 = pp.getBg().getG1();
        var groupG2 = pp.getBg().getG2();

        this.dsid = groupG1.restoreElement(listRepr.get(0));
        this.c0 = zn.restoreElement(listRepr.get(1));
        this.c1 = zn.restoreElement(listRepr.get(2));
        this.cPre0 = groupG1.restoreElement(listRepr.get(3));
        this.cPre1 = groupG1.restoreElement(listRepr.get(4));
        this.commitmentC0 = groupG1.restoreElement(listRepr.get(5));
        this.cTrace0 = groupG1.restoreVector(listRepr.get(6));
        this.cTrace1 = groupG1.restoreVector(listRepr.get(7));

        var gamma = Util.hashGamma(zn, dsid, tid, cPre0, cPre1, userChoice);
        var spendDeductCommonInput = new SpendDeductZkpCommonInput(gamma, c0, c1, dsid, cPre0, cPre1, commitmentC0, cTrace0, cTrace1);
        this.spendDeductZkp = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, listRepr.get(8));
        this.sigma = new SPSEQSignature(listRepr.get(9), groupG1, groupG2);
    }

    public SpendRequest(GroupElement dsid, FiatShamirProof spendDeductZkp, Zn.ZnElement c0, Zn.ZnElement c1, GroupElement cPre0, GroupElement cPre1, GroupElementVector cTrace0, GroupElementVector cTrace1, GroupElement commitmentC0, SPSEQSignature sigma) {
        this.dsid = dsid;
        this.spendDeductZkp = spendDeductZkp;
        this.c0 = c0;
        this.c1 = c1;
        this.cPre0 = cPre0;
        this.cPre1 = cPre1;
        this.cTrace0 = cTrace0;
        this.cTrace1 = cTrace1;
        this.commitmentC0 = commitmentC0;
        this.sigma = sigma;
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                dsid.getRepresentation(),
                c0.getRepresentation(),
                c1.getRepresentation(),
                cPre0.getRepresentation(),
                cPre1.getRepresentation(),
                commitmentC0.getRepresentation(),
                cTrace0.getRepresentation(),
                cTrace1.getRepresentation(),
                spendDeductZkp.getRepresentation(),
                sigma.getRepresentation()
        );
    }

    public GroupElement getDsid() {
        return this.dsid;
    }

    public FiatShamirProof getSpendDeductZkp() {
        return this.spendDeductZkp;
    }

    public Zn.ZnElement getC0() {
        return this.c0;
    }

    public Zn.ZnElement getC1() {
        return this.c1;
    }

    public GroupElement getCPre0() {
        return this.cPre0;
    }

    public GroupElement getCPre1() {
        return this.cPre1;
    }

    public GroupElementVector getCTrace0() {
        return this.cTrace0;
    }

    public GroupElementVector getCTrace1() {
        return this.cTrace1;
    }

    public GroupElement getCommitmentC0() {
        return this.commitmentC0;
    }

    public SPSEQSignature getSigma() {
        return this.sigma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendRequest that = (SpendRequest) o;
        return Objects.equals(dsid, that.dsid) && Objects.equals(spendDeductZkp, that.spendDeductZkp) && Objects.equals(c0, that.c0) && Objects.equals(c1, that.c1) && Objects.equals(cPre0, that.cPre0) && Objects.equals(cPre1, that.cPre1) && Objects.equals(cTrace0, that.cTrace0) && Objects.equals(cTrace1, that.cTrace1) && Objects.equals(commitmentC0, that.commitmentC0) && Objects.equals(sigma, that.sigma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dsid, spendDeductZkp, c0, c1, cPre0, cPre1, cTrace0, cTrace1, commitmentC0, sigma);
    }

    public String toString() {
        return "SpendRequest(dsid=" + this.getDsid() + ", spendDeductZkp=" + this.getSpendDeductZkp() + ", c0=" + this.getC0() + ", c1=" + this.getC1() + ", cPre0=" + this.getCPre0() + ", cPre1=" + this.getCPre1() + ", cTrace0=" + this.getCTrace0() + ", cTrace1=" + this.getCTrace1() + ", commitmentC0=" + this.getCommitmentC0() + ", sigma=" + this.getSigma() + ")";
    }
}
