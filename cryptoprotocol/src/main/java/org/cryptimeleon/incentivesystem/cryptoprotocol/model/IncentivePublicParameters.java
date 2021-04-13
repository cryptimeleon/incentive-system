package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.EqualsAndHashCode;
import org.cryptimeleon.craco.common.PublicParameters;
import org.cryptimeleon.craco.prf.zn.HashThenPrfToZn;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.setmembership.SetMembershipPublicParameters;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;


/**
 * A class representing the public parameters of the 2020 incentive system
 */
@EqualsAndHashCode
public class IncentivePublicParameters implements PublicParameters {
    @Represented
    private BilinearGroup bg;

    @Represented(restorer = "bg::getG1")
    private GroupElement w;

    @Represented(restorer = "bg::getG1")
    private GroupElement h7;

    @Represented(restorer = "bg::getG1")
    private GroupElement g1;

    @Represented(restorer = "bg::getG2")
    private GroupElement g2;

    @Represented
    private HashThenPrfToZn prfToZn; // not in paper, but we need to store PRF that is used in incentive system instance somewhere

    @Represented
    private SPSEQSignatureScheme spsEq; // same here for SPS-EQ scheme

    @Represented(restorer = "bg::getZn")
    private Zn.ZnElement eskDecBase; // TODO which value to choose? Check BA?

    @Represented
    private Integer maxPointBasePower; // eskDecBase^this determines the maximum point count that is considered valid

    private SetMembershipPublicParameters eskBaseSetMembershipPublicParameters;

    private int numEskDigits;

    public IncentivePublicParameters(Representation repr) {
        new ReprUtil(this)
                .deserialize(repr.list().get(0));
        this.eskBaseSetMembershipPublicParameters = new SetMembershipPublicParameters(bg, repr.list().get(1));
        init();
    }

    @Override
    public Representation getRepresentation() {
        var repr = new ListRepresentation();
        repr.add(ReprUtil.serialize(this));
        repr.add(eskBaseSetMembershipPublicParameters.getRepresentation());
        return repr;
    }

    public IncentivePublicParameters(BilinearGroup bg, GroupElement w, GroupElement h7, GroupElement g1, GroupElement g2, HashThenPrfToZn prfToZn, SPSEQSignatureScheme spsEq, Zn.ZnElement eskDecBase, int maxPointBasePower, SetMembershipPublicParameters eskBaseSetMembershipPublicParameters) {
        this.bg = bg;
        this.w = w;
        this.h7 = h7;
        this.g1 = g1;
        this.g2 = g2;
        this.prfToZn = prfToZn;
        this.spsEq = spsEq;
        this.eskDecBase = eskDecBase;
        this.maxPointBasePower = maxPointBasePower;
        this.eskBaseSetMembershipPublicParameters = eskBaseSetMembershipPublicParameters;
        init();
    }

    /**
     * Some initialization of redundant values
     */
    private void init() {
        numEskDigits = IntegerRing.decomposeIntoDigits(bg.getZn().getCharacteristic(), eskDecBase.getInteger()).length;
    }

    public BilinearGroup getBg() {
        return bg;
    }

    public GroupElement getW() {
        return w;
    }

    public GroupElement getH7() {
        return h7;
    }

    public GroupElement getG1() {
        return g1;
    }

    public GroupElement getG2() {
        return g2;
    }

    public HashThenPrfToZn getPrfToZn() {
        return prfToZn;
    }

    public void setPrfToZn(HashThenPrfToZn prfToZn) {
        this.prfToZn = prfToZn;
    }

    public SPSEQSignatureScheme getSpsEq() {
        return spsEq;
    }

    public Zn.ZnElement getEskDecBase() {
        return eskDecBase;
    }

    public int getNumEskDigits() {
        return numEskDigits;
    }

    public int getMaxPointBasePower() {
        return maxPointBasePower;
    }

    public SetMembershipPublicParameters getEskBaseSetMembershipPublicParameters() {
        return eskBaseSetMembershipPublicParameters;
    }
}
