package org.cryptimeleon.incentive.promotion.reward;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RewardSideEffectTest {

    @Test
    void testRepresentation() {
        RewardSideEffect rewardSideEffect = new RewardSideEffect("Free Teddy");
        RewardSideEffect deserializedRewardSideEffect = new RewardSideEffect(rewardSideEffect.getRepresentation());
        assertEquals(rewardSideEffect, deserializedRewardSideEffect);
    }

}