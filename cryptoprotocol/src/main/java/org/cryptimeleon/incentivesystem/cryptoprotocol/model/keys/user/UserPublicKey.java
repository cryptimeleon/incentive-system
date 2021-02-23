package org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user;

import org.cryptimeleon.math.structures.groups.GroupElement;

public class UserPublicKey {
    private GroupElement upk;

    public UserPublicKey(GroupElement upk) {
        this.upk = upk;
    }
}
