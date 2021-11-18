package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.hash.annotations.UniqueByteRepresented;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * This class contains promotion-specific data.
 */
@Value
@AllArgsConstructor
public class PromotionParameters implements Representable {

    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    Zn.ZnElement promotionId; // or some other fixed identifier

    @NonFinal
    @Represented
    Integer pointsVectorSize;


    public PromotionParameters(Zn zn, Representation repr) {
        new ReprUtil(this).register(zn, "Zn").deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
