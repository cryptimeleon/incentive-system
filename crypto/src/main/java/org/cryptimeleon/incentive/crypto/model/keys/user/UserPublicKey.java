package org.cryptimeleon.incentive.crypto.model.keys.user;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.util.Objects;

public class UserPublicKey implements Representable {

    @Represented(restorer = "G1")
    GroupElement upk;

    public UserPublicKey(GroupElement upk) {
        this.upk = upk;
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

    public GroupElement getUpk() {
        return this.upk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPublicKey that = (UserPublicKey) o;
        return Objects.equals(upk, that.upk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upk);
    }
}
