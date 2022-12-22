package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;

/**
 * Class for sending no metadata with an update.
 * Chosen instead of using null since null could also be caused by an error.
 * <p>
 * This class should function like a Kotlin object, all instances are equal.
 */
public class EmptyTokenUpdateMetadata extends ZkpTokenUpdateMetadata {
    public EmptyTokenUpdateMetadata() {
    }

    public EmptyTokenUpdateMetadata(Representation representation) {
        ReprUtil.deserialize(this, representation);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj != null && getClass() == obj.getClass();
    }

    @Override
    public int hashCode() {
        return 104237508;
    }
}
