package org.cryptimeleon.incentive.promotion;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;

/**
 * Class for sending no metadata with an update.
 * Chosen instead of using null since null could also be caused by an error.
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
}