package org.cryptimeleon.incentive.promotion;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

/**
 * Class that represents side effects on baskets, e.g. a reward that is added to the basket, a discount, ...
 * This will probably be updated to a smarter version in the near future.
 */
@Value
@AllArgsConstructor
public class RewardSideEffect implements StandaloneRepresentable {

    @Represented
    @NonFinal
    public String name;

    public RewardSideEffect(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
