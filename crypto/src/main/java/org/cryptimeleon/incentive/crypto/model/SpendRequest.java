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
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;

/**
 * Data class for the request sent by a user in spend-deduct.
 */
public class SpendRequest implements Representable {
    private final Zn.ZnElement dsid;

    private final FiatShamirProof spendDeductZkp;

    private final Zn.ZnElement c;

    private final GroupElement cPre0;

    private final GroupElement cPre1;
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

        this.dsid = zn.restoreElement(listRepr.get(0));
        this.c = zn.restoreElement(listRepr.get(1));
        this.cPre0 = groupG1.restoreElement(listRepr.get(2));
        this.cPre1 = groupG1.restoreElement(listRepr.get(3));
        this.commitmentC0 = groupG1.restoreElement(listRepr.get(4));

        var gamma = Util.hashGammaOld(zn, dsid, tid, cPre0, cPre1, userChoice);
        var spendDeductCommonInput = new SpendDeductZkpCommonInput(gamma, c, dsid, cPre0, cPre1, commitmentC0);
        this.spendDeductZkp = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, listRepr.get(5));
        this.sigma = new SPSEQSignature(listRepr.get(6), groupG1, groupG2);
    }

    public SpendRequest(Zn.ZnElement dsid, FiatShamirProof spendDeductZkp, Zn.ZnElement c, GroupElement cPre0, GroupElement cPre1, GroupElement commitmentC0, SPSEQSignature sigma) {
        this.dsid = dsid;
        this.spendDeductZkp = spendDeductZkp;
        this.c = c;
        this.cPre0 = cPre0;
        this.cPre1 = cPre1;
        this.commitmentC0 = commitmentC0;
        this.sigma = sigma;
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                dsid.getRepresentation(),
                c.getRepresentation(),
                cPre0.getRepresentation(),
                cPre1.getRepresentation(),
                commitmentC0.getRepresentation(),
                spendDeductZkp.getRepresentation(),
                sigma.getRepresentation()
        );
    }

    public Zn.ZnElement getDsid() {
        return this.dsid;
    }

    public FiatShamirProof getSpendDeductZkp() {
        return this.spendDeductZkp;
    }

    public Zn.ZnElement getC() {
        return c;
    }

    public GroupElement getCPre0() {
        return this.cPre0;
    }

    public GroupElement getCPre1() {
        return this.cPre1;
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
        return Objects.equals(dsid, that.dsid) && Objects.equals(spendDeductZkp, that.spendDeductZkp) && Objects.equals(c, that.c) && Objects.equals(cPre0, that.cPre0) && Objects.equals(cPre1, that.cPre1) && Objects.equals(commitmentC0, that.commitmentC0) && Objects.equals(sigma, that.sigma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dsid, spendDeductZkp, c, cPre0, cPre1, commitmentC0, sigma);
    }
}
