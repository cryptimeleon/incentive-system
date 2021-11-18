package org.cryptimeleon.incentive.promotion.promotions;

import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
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
        IncentivePublicParameters publicParameters = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
        PromotionParameters promotionParameters = NutellaPromotion.generatePromotionParameters();
        List<Reward> rewardList = List.of(new NutellaReward(4, UUID.randomUUID(), new RewardSideEffect("Free Nutella")));
        NutellaPromotion nutellaPromotion = new NutellaPromotion(promotionParameters, rewardList);
        NutellaPromotion deserializedNutellaPromotion = new NutellaPromotion(nutellaPromotion.getRepresentation());

        assertEquals(nutellaPromotion, deserializedNutellaPromotion);
    }
}