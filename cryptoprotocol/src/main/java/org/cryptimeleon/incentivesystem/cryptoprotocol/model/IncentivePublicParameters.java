package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.common.PublicParameters;
import org.cryptimeleon.craco.prf.aes.AesPseudorandomFunction;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;

/**
 * A class representing the public parameters of the 2020 incentive system
 */
@Value
@AllArgsConstructor
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
    @Represented(restorer = "bg::getG1")
    GroupElement g1;

    @NonFinal
    @Represented(restorer = "bg::getG2")
    GroupElement g2;

    @NonFinal
    @Represented
    AesPseudorandomFunction prf; // not in paper, but we need to store PRF that is used in incentive system instance somewhere

    @Represented
    @NonFinal
    SPSEQSignatureScheme spsEq; // same here for SPS-EQ scheme

    public IncentivePublicParameters(Representation repr) {
        new ReprUtil(this)
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
