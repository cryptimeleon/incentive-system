package org.cryptimeleon.incentive.promotion.sideeffect;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.util.Objects;

/**
 * Class that represents side effects on baskets in form of free reward items.
 */
public final class RewardSideEffect extends SideEffect {

    @Represented
    public String rewardId;

    public RewardSideEffect(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    public RewardSideEffect(String rewardId) {
        this.rewardId = rewardId;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public String getRewardId() {
        return this.rewardId;
    }

    public String toString() {
        return "RewardSideEffect(rewardId=" + this.getRewardId() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewardSideEffect that = (RewardSideEffect) o;
        return Objects.equals(rewardId, that.rewardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rewardId);
    }
}
