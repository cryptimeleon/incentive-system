package org.cryptimeleon.incentive.promotion.reward;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutellaRewardTest {

    @Test
    public void representationTest() {
        NutellaReward nutellaReward = new NutellaReward(8, UUID.randomUUID(), new RewardSideEffect("Free Nutella"));
        NutellaReward deserializedNutellaReward = new NutellaReward(nutellaReward.getRepresentation());
        assertEquals(nutellaReward, deserializedNutellaReward);
    }
}