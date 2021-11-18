package org.cryptimeleon.incentive.promotion.reward;

import lombok.EqualsAndHashCode;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

@EqualsAndHashCode
public class RewardSideEffect implements Representable {

    @Represented
    public String name;

    public RewardSideEffect(String name) {
        this.name = name;
    }

    public RewardSideEffect(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
