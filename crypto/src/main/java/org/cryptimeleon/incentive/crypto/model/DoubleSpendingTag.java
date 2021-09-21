package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data associated to a spend operation which the provider requires to trace double-spending.
 * Called 'dstag' in the cryptimeleon incentive system paper.
 */
@Value
@AllArgsConstructor
public class DoubleSpendingTag implements Representable {
    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement c0; // challenge for deriving the user secret key

    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement c1; // challenge for deriving the encryption secret key

    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement gamma; // challenge generation helper value

    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement eskStarProv; // provider share for ElGamal encryption secret key

    @NonFinal
    @Represented(restorer = "G1")
    GroupElementVector ctrace0;

    @NonFinal
    @Represented(restorer = "G1")
    GroupElementVector ctrace1;

    public DoubleSpendingTag(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").register(pp.getBg().getG1(), "G1").deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
