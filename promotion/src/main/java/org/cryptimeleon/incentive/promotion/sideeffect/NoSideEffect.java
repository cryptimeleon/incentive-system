package org.cryptimeleon.incentive.promotion.sideeffect;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;

/**
 * Class for ZKPs with no side effects i.e. ZKPs needed for more complicated token updates than adding a public point
 * vector.
 */
public final class NoSideEffect extends SideEffect {
    public NoSideEffect() {
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public String toString() {
        return "NoSideEffect()";
    }

    @Override
    public int hashCode() {
        return 935728995;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass();
    }
}
