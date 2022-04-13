package org.cryptimeleon.incentive.promotion.streak;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * User metadata for timestamps. This ensures that the user's public input is known to the verifier and avoids errors
 * due to edge cases (like buying something around midnight), and is required with more precise (e.g. seconds) timestamps.
 *
 * We use an epoch day timestamp, but for other implementations minutes or seconds might be required.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class StreakTokenUpdateTimestamp extends ZkpTokenUpdateMetadata {

    @Represented
    private Long timestamp;

    StreakTokenUpdateTimestamp(Long epochTimeStamp) {
        this.timestamp = epochTimeStamp;
    }

    public StreakTokenUpdateTimestamp(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    /**
     * Compute a timestamp metadata for the current time/day.
     * @return timestamp
     */
    public static StreakTokenUpdateTimestamp now() {
        LocalDate epoch = LocalDate.ofEpochDay(0);
        LocalDate now = LocalDate.now();
        return new StreakTokenUpdateTimestamp(ChronoUnit.DAYS.between(epoch, now));
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
