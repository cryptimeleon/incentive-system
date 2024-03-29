package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpCommonInput;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;
import java.util.UUID;

public class SpendStoreRequest implements Representable {
    private final Zn.ZnElement dsid;
    private final Zn.ZnElement c;
    private final SPSEQSignature sigma;
    private final GroupElement c0; // do not send C_1 since it should be equal to g_1 anyways
    private final GroupElement cPre0;
    private final GroupElement cPre1;
    private final FiatShamirProof spendZkp;

    public SpendStoreRequest(Representation representation, IncentivePublicParameters pp, UUID basketId, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey, SpendDeductTree spendDeductTree, UniqueByteRepresentable context) {
        ListRepresentation listRepresentation = (ListRepresentation) representation;
        Group g1 = pp.getBg().getG1();

        this.dsid = pp.getBg().getZn().restoreElement(listRepresentation.get(0));
        this.c = pp.getBg().getZn().restoreElement(listRepresentation.get(1));
        this.sigma = pp.getSpsEq().restoreSignature(listRepresentation.get(2));
        this.c0 = g1.restoreElement(listRepresentation.get(3));
        this.cPre0 = g1.restoreElement(listRepresentation.get(4));
        this.cPre1 = g1.restoreElement(listRepresentation.get(5));

        // Kinda nasty deserialization of zkp
        var gamma = Util.hashGamma(pp.getBg().getZn(), dsid, basketId, cPre0, cPre1, cPre1.pow(promotionParameters.getPromotionId()), context);
        var spendDeductCommonInput = new SpendDeductZkpCommonInput(gamma, c, dsid, cPre0, cPre1, c0);
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey));
        this.spendZkp = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, listRepresentation.get(6));
    }

    public SpendStoreRequest(Zn.ZnElement dsid, Zn.ZnElement c, SPSEQSignature sigma, GroupElement c0, GroupElement cPre0, GroupElement cPre1, FiatShamirProof spendZkp) {
        this.dsid = dsid;
        this.c = c;
        this.sigma = sigma;
        this.c0 = c0;
        this.cPre0 = cPre0;
        this.cPre1 = cPre1;
        this.spendZkp = spendZkp;
    }

    public Zn.ZnElement getDsid() {
        return dsid;
    }

    public Zn.ZnElement getC() {
        return c;
    }

    public SPSEQSignature getSigma() {
        return sigma;
    }

    public GroupElement getC0() {
        return c0;
    }

    public GroupElement getCPre0() {
        return cPre0;
    }

    public GroupElement getCPre1() {
        return cPre1;
    }

    public FiatShamirProof getSpendZkp() {
        return spendZkp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendStoreRequest that = (SpendStoreRequest) o;
        return Objects.equals(dsid, that.dsid) && Objects.equals(c, that.c) && Objects.equals(sigma, that.sigma) && Objects.equals(c0, that.c0) && Objects.equals(cPre0, that.cPre0) && Objects.equals(cPre1, that.cPre1) && Objects.equals(spendZkp, that.spendZkp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dsid, c, sigma, c0, cPre0, cPre1, spendZkp);
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                dsid.getRepresentation(),
                c.getRepresentation(),
                sigma.getRepresentation(),
                c0.getRepresentation(),
                cPre0.getRepresentation(),
                cPre1.getRepresentation(),
                spendZkp.getRepresentation()
        );
    }
}
