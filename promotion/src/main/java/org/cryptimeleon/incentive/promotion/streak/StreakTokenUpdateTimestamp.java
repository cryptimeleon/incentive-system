package org.cryptimeleon.incentive.promotion.streak;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@EqualsAndHashCode(callSuper = true)
@Getter
public class StreakTokenUpdateTimestamp extends ZkpTokenUpdateMetadata {

    @Represented
    private Long timestamp;

    StreakTokenUpdateTimestamp(Long epochTimeStamp) {
        this.timestamp = epochTimeStamp;
    }

    StreakTokenUpdateTimestamp(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    static StreakTokenUpdateTimestamp now() {
        LocalDate epoch = LocalDate.ofEpochDay(0);
        LocalDate now = LocalDate.ofEpochDay(0);
        return new StreakTokenUpdateTimestamp(ChronoUnit.DAYS.between(epoch, now));
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
