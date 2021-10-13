package org.cryptimeleon.incentive.promotion;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class GreaterEqualCondition extends Condition {
    long value;

    @Override
    String getDescription() {
        return "more than " + value + " points";
    }
}
