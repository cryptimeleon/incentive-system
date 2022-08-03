package org.cryptimeleon.incentive.crypto.model;

import lombok.Value;
import lombok.experimental.NonFinal;
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


/**
 * A class representing the public parameters of the 2020 incentive system.
 */
@Value
public class IncentivePublicParameters implements PublicParameters {
    @NonFinal
    @Represented
    BilinearGroup bg;

    @NonFinal
    @Represented(restorer = "bg::getG1")
    GroupElement g1Generator;

    @NonFinal
    @Represented(restorer = "bg::getG2")
    GroupElement g2Generator;

    @NonFinal
    @Represented(restorer = "bg::getG1")
    GroupElement w;

    @NonFinal
    @Represented(restorer = "bg::getG1")
    GroupElement h7;

    @NonFinal
    @Represented
    HashThenPrfToZn prfToZn; // not in paper, but we need to store PRF that is used in incentive system instance somewhere

    @NonFinal
    @Represented
    SPSEQSignatureScheme spsEq; // same here for SPS-EQ scheme

    @NonFinal
    @Represented(restorer = "bg::getZn")
    Zn.ZnElement eskDecBase;

    @NonFinal
    @Represented
    Integer maxPointBasePower; // eskDecBase^this determines the maximum point count that is considered valid

    @NonFinal
    SetMembershipPublicParameters eskBaseSetMembershipPublicParameters;

    @NonFinal
    int numEskDigits; // rho from the 2020 inc sys paper (number of digits of esk in base-representation), this is computed in the init method since it contains redundant data

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
}
