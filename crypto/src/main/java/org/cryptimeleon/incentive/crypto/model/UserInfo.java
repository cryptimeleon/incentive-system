package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data class storing info about a user that spent a specific token in a specific transaction.
 * This info is associated with a token (represented by a dsid).
 **/
@Getter
@Setter
@AllArgsConstructor
public class UserInfo implements Representable {
    @NonFinal
    private UserPublicKey upk;

    @NonFinal
    @Represented(restorer = "Zn")
    private Zn.ZnElement dsBlame;

    @NonFinal
    @Represented(restorer = "Zn")
    private Zn.ZnElement dsTrace;

    public UserInfo (Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").deserialize(repr.list().get(0));
        this.upk = new UserPublicKey(repr.list().get(1), pp.getBg().getG1());
    }

    @Override
    public Representation getRepresentation() {
        ListRepresentation repr = new ListRepresentation();

        repr.add(ReprUtil.serialize(this));
        repr.add(this.upk.getRepresentation());

        return repr;
    }

    @Override
    public boolean equals(Object o) {
        if(!o.getClass().equals(UserInfo.class)) {
            return false;
        }
        else {
            UserInfo otherUserInfo = (UserInfo) o;
            return otherUserInfo.dsBlame.equals(this.dsBlame)
                    && otherUserInfo.dsTrace.equals(this.dsTrace)
                    && otherUserInfo.upk.equals(this.upk);
        }
    }

    @Override
    public String toString() {
        return this.upk.toString() + " "
                + this.dsBlame.toString() + " "
                + this.dsTrace.toString();
    }

}
