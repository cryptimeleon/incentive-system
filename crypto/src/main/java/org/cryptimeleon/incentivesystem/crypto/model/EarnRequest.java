package org.cryptimeleon.incentivesystem.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;

/**
 * Data class for the request sent in the credit-earn protocol.
 */
@Value
@AllArgsConstructor
public class EarnRequest implements Representable {
    @NonFinal
    @Represented(restorer = "SPSEQ")
    SPSEQSignature blindedSignature;

    @NonFinal
    @Represented(restorer = "G1")
    GroupElement c0; // first element of the tuple C

    @NonFinal
    @Represented(restorer = "G1")
    GroupElement c1; // second element of the tuple C

    public EarnRequest(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getBg().getG1(), "G1")
                .register(pp.getSpsEq(), "SPSEQ")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
