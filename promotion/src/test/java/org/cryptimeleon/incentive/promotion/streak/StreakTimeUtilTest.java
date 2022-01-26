package org.cryptimeleon.incentive.promotion.streak;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StreakTimeUtilTest {

    /**
     * Assert Time Util does not throw any errors and returns a somewhat sensible result.
     */
    @Test
    void testTimeUtil() {
        StreakTimeUtil streakTimeUtil = new StreakTimeUtil();
        Assertions.assertTrue(19000 < streakTimeUtil.getTodayAsEpochDay());
    }
}