package org.cryptimeleon.incentive.promotion.streak;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StreakTokenUpdateTimestampTest {

    @Test
    void testNow() {
        assertTrue(StreakTokenUpdateTimestamp.now().getTimestamp() > 0);
    }

}