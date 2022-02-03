package org.cryptimeleon.incentive.promotion.streak;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class StreakPromotionTest {

    StreakPromotion streakPromotion = new StreakPromotion(
            StreakPromotion.generatePromotionParameters(),
            "Test Streak Promotion",
            "This is a test promotion",
            List.of(),
            7
    );

    @Test
    void doNotAllowEarnProtocol() {
        assertFalse(streakPromotion.getFastEarnSupported());
    }
}
