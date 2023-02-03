package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;

/**
 * Data class storing info about a user that spent a specific token in a specific transaction.
 * This info is associated with a token (represented by a dsid).
 **/
public class UserInfo implements Representable {
    private final UserPublicKey upk;

    @Represented(restorer = "Zn")
    private Zn.ZnElement dsBlame;

    public UserInfo(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").deserialize(repr.list().get(0));
        this.upk = new UserPublicKey(repr.list().get(1), pp);
    }

    public UserInfo(UserPublicKey upk, Zn.ZnElement dsBlame) {
        this.upk = upk;
        this.dsBlame = dsBlame;
    }

    @Override
    public Representation getRepresentation() {
        ListRepresentation repr = new ListRepresentation();

        repr.add(ReprUtil.serialize(this));
        repr.add(this.upk.getRepresentation());

        return repr;
    }

    @Override
    public String toString() {
        return this.upk.toString() + " "
                + this.dsBlame.toString();
    }

    public UserPublicKey getUpk() {
        return this.upk;
    }

    public Zn.ZnElement getDsBlame() {
        return this.dsBlame;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(upk, userInfo.upk) && Objects.equals(dsBlame, userInfo.dsBlame);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upk, dsBlame);
    }
}
