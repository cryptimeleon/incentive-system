package de.upb.crypto.incentive.cryptoprotocol.model.keys.user;

import de.upb.crypto.math.interfaces.structures.GroupElement;

public class UserPublicKey
{
    private GroupElement upk;

    public UserPublicKey(GroupElement upk)
    {
        this.upk = upk;
    }
}
