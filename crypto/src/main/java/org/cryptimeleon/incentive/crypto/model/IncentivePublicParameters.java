package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.common.PublicParameters;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.setmembership.SetMembershipPublicParameters;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.math.prf.zn.HashThenPrfToZn;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;


/**
 * A class representing the public parameters of the 2020 incentive system.
 */
public class IncentivePublicParameters implements PublicParameters {
    private final SetMembershipPublicParameters eskBaseSetMembershipPublicParameters;
    @Represented
    private BilinearGroup bg;
    @Represented(restorer = "bg::getG1")
    private GroupElement g1Generator;
    @Represented(restorer = "bg::getG2")
    private GroupElement g2Generator;
    @Represented(restorer = "bg::getG1")
    private GroupElement w;
    @Represented(restorer = "bg::getG1")
    private GroupElement h7;
    @Represented
    private HashThenPrfToZn prfToZn; // not in paper, but we need to store PRF that is used in incentive system instance somewhere
    @Represented
    private SPSEQSignatureScheme spsEq; // same here for SPS-EQ scheme
    @Represented(restorer = "bg::getZn")
    private Zn.ZnElement eskDecBase;
    @Represented
    private Integer maxPointBasePower; // eskDecBase^this determines the maximum point count that is considered valid
    private int numEskDigits; // rho from the 2020 inc sys paper (number of digits of esk in base-representation), this is computed in the init method since it contains redundant data

    public IncentivePublicParameters(Representation repr) {
        new ReprUtil(this)
                .deserialize(repr.list().get(0));
        this.eskBaseSetMembershipPublicParameters = new SetMembershipPublicParameters(bg, repr.list().get(1));
        init();
    }

    public IncentivePublicParameters(BilinearGroup bg,
                                     GroupElement w,
                                     GroupElement h7,
                                     GroupElement g1,
                                     GroupElement g2,
                                     HashThenPrfToZn prfToZn,
                                     SPSEQSignatureScheme spsEq,
                                     Zn.ZnElement eskDecBase,
                                     int maxPointBasePower,
                                     SetMembershipPublicParameters eskBaseSetMembershipPublicParameters) {
        this.bg = bg;
        this.w = w;
        this.h7 = h7;
        this.g1Generator = g1;
        this.g2Generator = g2;
        this.prfToZn = prfToZn;
        this.spsEq = spsEq;
        this.eskDecBase = eskDecBase;
        this.maxPointBasePower = maxPointBasePower;
        this.eskBaseSetMembershipPublicParameters = eskBaseSetMembershipPublicParameters;
        init();
    }

    @Override
    public Representation getRepresentation() {
        var repr = new ListRepresentation();
        repr.add(ReprUtil.serialize(this));
        repr.add(eskBaseSetMembershipPublicParameters.getRepresentation());
        return repr;
    }

    /**
     * Some initialization of redundant values
     */
    private void init() {
        numEskDigits = IntegerRing.decomposeIntoDigits(bg.getZn().getCharacteristic(), eskDecBase.asInteger()).length;
    }

    public BilinearGroup getBg() {
        return this.bg;
    }

    public GroupElement getG1Generator() {
        return this.g1Generator;
    }

    public GroupElement getG2Generator() {
        return this.g2Generator;
    }

    public GroupElement getW() {
        return this.w;
    }

    public GroupElement getH7() {
        return this.h7;
    }

    public HashThenPrfToZn getPrfToZn() {
        return this.prfToZn;
    }

    public SPSEQSignatureScheme getSpsEq() {
        return this.spsEq;
    }

    public Zn.ZnElement getEskDecBase() {
        return this.eskDecBase;
    }

    public Integer getMaxPointBasePower() {
        return this.maxPointBasePower;
    }

    public SetMembershipPublicParameters getEskBaseSetMembershipPublicParameters() {
        return this.eskBaseSetMembershipPublicParameters;
    }

    public int getNumEskDigits() {
        return this.numEskDigits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncentivePublicParameters that = (IncentivePublicParameters) o;
        return numEskDigits == that.numEskDigits && Objects.equals(bg, that.bg) && Objects.equals(g1Generator, that.g1Generator) && Objects.equals(g2Generator, that.g2Generator) && Objects.equals(w, that.w) && Objects.equals(h7, that.h7) && Objects.equals(prfToZn, that.prfToZn) && Objects.equals(spsEq, that.spsEq) && Objects.equals(eskDecBase, that.eskDecBase) && Objects.equals(maxPointBasePower, that.maxPointBasePower) && Objects.equals(eskBaseSetMembershipPublicParameters, that.eskBaseSetMembershipPublicParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bg, g1Generator, g2Generator, w, h7, prfToZn, spsEq, eskDecBase, maxPointBasePower, eskBaseSetMembershipPublicParameters, numEskDigits);
    }

    public String toString() {
        return "IncentivePublicParameters(bg=" + this.getBg() + ", g1Generator=" + this.getG1Generator() + ", g2Generator=" + this.getG2Generator() + ", w=" + this.getW() + ", h7=" + this.getH7() + ", prfToZn=" + this.getPrfToZn() + ", spsEq=" + this.getSpsEq() + ", eskDecBase=" + this.getEskDecBase() + ", maxPointBasePower=" + this.getMaxPointBasePower() + ", eskBaseSetMembershipPublicParameters=" + this.getEskBaseSetMembershipPublicParameters() + ", numEskDigits=" + this.getNumEskDigits() + ")";
    }
}
