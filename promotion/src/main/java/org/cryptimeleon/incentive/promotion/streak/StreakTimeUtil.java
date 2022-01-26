package org.cryptimeleon.incentive.promotion.streak;

import java.time.LocalDate;
import java.time.ZoneId;


/**
 * Utility class for getting the current day.
 */
public class StreakTimeUtil {

    // Central European Time
    final String CRYPTIMELEON_ZONE = "CET";

    public StreakTimeUtil() {

    }

    /**
     * Get today's date.
     *
     * @return date in LocalDate format
     */
    private LocalDate getToday() {
        return LocalDate.now(ZoneId.of(CRYPTIMELEON_ZONE));
    }

    /**
     * Get today's date in Epoch Day format.
     * Incremented from 0 which is 1970-01-01. Allows simple computation of day difference of two dates.
     *
     * @return Today's Epoch Day
     */
    long getTodayAsEpochDay() {
        return getToday().toEpochDay();
    }
}
