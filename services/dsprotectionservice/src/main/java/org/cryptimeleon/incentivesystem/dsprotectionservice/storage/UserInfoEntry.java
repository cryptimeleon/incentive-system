package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import javax.persistence.*;


/**
 * Class storing some anonymous information about a user behind a token.
 * This includes the user's public key and the dsblame and dstrace information for that token.
 */

@Entity
@Table(name="userInfo")
public class UserInfoEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private UserPublicKey upk;

    private Zn.ZnElement dsBlame;

    private Zn.ZnElement dsTrace;

    public UserInfoEntry() {}

    public UserInfoEntry(long id, UserPublicKey upk, Zn.ZnElement dsBlame, Zn.ZnElement dsTrace) {
        this.id = id;
        this.upk = upk;
        this.dsBlame = dsBlame;
        this.dsTrace = dsTrace;
    }
}
