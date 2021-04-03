package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.Util;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductCommonInput;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

@Value
@AllArgsConstructor
public class SpendRequest implements Representable {
    @NonFinal
    GroupElement dsid;

    @NonFinal
    FiatShamirProof spendDeductZkp;

    @NonFinal
    Zn.ZnElement c0;

    @NonFinal
    Zn.ZnElement c1;

    @NonFinal
    GroupElement cPre0;

    @NonFinal
    GroupElement cPre1;

    @NonFinal
    GroupElementVector ctrace0;

    @NonFinal
    GroupElementVector ctrace1;

    @NonFinal
    GroupElement commitmentC0;

    @NonFinal
    GroupElement commitmentC1;

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                dsid.getRepresentation(),
                c0.getRepresentation(),
                c1.getRepresentation(),
                cPre0.getRepresentation(),
                cPre1.getRepresentation(),
                commitmentC0.getRepresentation(),
                commitmentC1.getRepresentation(),
                ctrace0.getRepresentation(),
                ctrace1.getRepresentation(),
                spendDeductZkp.getRepresentation()
                );
    }

    public SpendRequest(Representation repr, IncentivePublicParameters pp, FiatShamirProofSystem fiatShamirProofSystem, BigInteger k, Zn.ZnElement tid) {
        var listRepr = repr.list();
        var zn = pp.getBg().getZn();
        var groupG1 = pp.getBg().getG1();

        this.dsid = pp.getBg().getG1().restoreElement(listRepr.get(0));
        this.c0 = zn.restoreElement(listRepr.get(1));
        this.c1 = zn.restoreElement(listRepr.get(2));
        this.cPre0 = groupG1.restoreElement(listRepr.get(3));
        this.cPre1 = groupG1.restoreElement(listRepr.get(4));
        this.commitmentC0 = groupG1.restoreElement(listRepr.get(5));
        this.commitmentC1 = groupG1.restoreElement(listRepr.get(6));
        this.ctrace0 = groupG1.restoreVector(listRepr.get(7));
        this.ctrace1 = groupG1.restoreVector(listRepr.get(8));

        var gamma = Util.hashGamma(zn, k, dsid, tid, cPre0, cPre1);
        var spendDeductCommonInput = new SpendDeductCommonInput(
                k, gamma, c0, c1, dsid, pp.getW(), cPre0, cPre1, commitmentC0, commitmentC1, ctrace0, ctrace1
        );
        this.spendDeductZkp = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, listRepr.get(9));

    }
}
