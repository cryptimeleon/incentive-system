package org.cryptimeleon.incentive.promotion.sideeffect;

import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SideEffectRepresentationTest {

    @Test
    void rewardSideEffectRepresentationTest() {
        RewardSideEffect rewardSideEffect = new RewardSideEffect("Free Teddy");
        RepresentableRepresentation rewardSideEffectRepr = new RepresentableRepresentation(rewardSideEffect);
        SideEffect deserializedRewardSideEffect = (SideEffect) rewardSideEffectRepr.recreateRepresentable();

        assertTrue(deserializedRewardSideEffect instanceof RewardSideEffect);
        assertEquals(rewardSideEffect, deserializedRewardSideEffect);

        NoSideEffect noSideEffect = new NoSideEffect();
        RepresentableRepresentation noSideEffectRepr = new RepresentableRepresentation(noSideEffect);
        SideEffect deserializedNoSideEffect = (SideEffect) noSideEffectRepr.recreateRepresentable();

        assertTrue(deserializedNoSideEffect instanceof NoSideEffect);
        assertEquals(noSideEffect, deserializedNoSideEffect);
    }
}
