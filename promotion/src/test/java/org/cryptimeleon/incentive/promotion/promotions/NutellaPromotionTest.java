package org.cryptimeleon.incentive.promotion.promotions;

import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.promotion.reward.NutellaReward;
import org.cryptimeleon.incentive.promotion.reward.Reward;
import org.cryptimeleon.incentive.promotion.reward.RewardSideEffect;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutellaPromotionTest {

    @Test
    void representationTest() {
        PromotionParameters promotionParameters = NutellaPromotion.generatePromotionParameters();
        List<Reward> rewardList = List.of(new NutellaReward(4, "Some reward", UUID.randomUUID(), new RewardSideEffect("Free Nutella")));
        NutellaPromotion nutellaPromotion = new NutellaPromotion(promotionParameters, "Test Promotion", "This is some Test Promotion", rewardList, "nutella");
        NutellaPromotion deserializedNutellaPromotion = new NutellaPromotion(nutellaPromotion.getRepresentation());

        assertEquals(nutellaPromotion, deserializedNutellaPromotion);
    }
}
