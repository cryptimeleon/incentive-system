package org.cryptimeleon.incentive.promotion;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class LessEqualCondition extends Condition {
    long value;

    @Override
    String getDescription() {
        return "less than " + value + " points";
    }
}
