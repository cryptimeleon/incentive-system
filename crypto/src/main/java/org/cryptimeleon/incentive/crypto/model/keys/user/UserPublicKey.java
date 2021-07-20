package org.cryptimeleon.incentive.crypto.model.keys.user;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;

@Value
public class UserPublicKey implements Representable {

    @NonFinal
    @Represented(restorer = "G1")
    GroupElement upk;

    public UserPublicKey(GroupElement upk) {
        this.upk = upk;
    }

    public UserPublicKey(Representation repr, Group g1) {
        new ReprUtil(this)
                .register(g1, "G1")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
