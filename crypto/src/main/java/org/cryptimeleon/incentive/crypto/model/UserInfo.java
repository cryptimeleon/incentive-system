package org.cryptimeleon.incentive.crypto.model;

import lombok.Value;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data class storing info about a user that spent a specific token in a specific transaction.
 * This info is associated with a token (represented by a dsid).
 **/
@Value
public class UserInfo {
    private UserPublicKey upk;
    private Zn.ZnElement dsBlame;
    private Zn.ZnElement dsTrace;
}
