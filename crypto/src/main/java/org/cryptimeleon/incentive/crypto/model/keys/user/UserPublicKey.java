package org.cryptimeleon.incentive.crypto.model.keys.user;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.NonFinal;

@Getter
@EqualsAndHashCode
public class UserPublicKey implements Representable {

    @NonFinal
    @Represented(restorer = "G1")
    GroupElement upk;

    public UserPublicKey(GroupElement upk) {
        this.upk = upk;
    }

    @Deprecated
    public UserPublicKey(Representation repr, Group g1) {
        new ReprUtil(this)
                .register(g1, "G1")
                .deserialize(repr);
    }

    public UserPublicKey(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getBg().getG1(), "G1")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public String toString() {
        return this.upk.toString();
    }
}
