package org.cryptimeleon.incentive.promotion.sideeffect;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;

/**
 * Class for ZKPs with no side effects i.e. ZKPs needed for more complicated token updates than adding a public point
 * vector.
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class NoSideEffect extends SideEffect {
    public NoSideEffect(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
