package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.math.BigInteger;

/**
 * This class contains promotion-specific data.
 */
@Value
@AllArgsConstructor
public class PromotionParameters implements StandaloneRepresentable{

    @NonFinal
    @Represented
    BigInteger promotionId; // or some other fixed identifier

    @NonFinal
    @Represented
    Integer pointsVectorSize;

    public PromotionParameters(Representation repr) {
        ReprUtil.deserialize(this, repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
