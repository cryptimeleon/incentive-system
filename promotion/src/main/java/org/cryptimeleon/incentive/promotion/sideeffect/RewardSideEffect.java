package org.cryptimeleon.incentive.promotion.sideeffect;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

/**
 * Class that represents side effects on baskets in form of free reward items.
 */
@Value
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class RewardSideEffect extends SideEffect {

    @Represented
    @NonFinal
    public String rewardId;

    public RewardSideEffect(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
