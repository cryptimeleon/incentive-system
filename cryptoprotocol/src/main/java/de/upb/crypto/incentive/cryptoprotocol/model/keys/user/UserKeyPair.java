package de.upb.crypto.incentive.cryptoprotocol.model.keys.user;

import lombok.Data;

@Data
public class UserKeyPair {
    private UserPublicKey userPublicKey;
    private UserSecretKey userSecretKey;

    public UserKeyPair(UserPublicKey upk, UserSecretKey usk) {
        this.userPublicKey = upk;
        this.userSecretKey = usk;
    }
}
