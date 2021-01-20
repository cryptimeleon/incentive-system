package de.upb.crypto.incentive.cryptoprotocol.model.keys.user;


public class UserKeyPair
{
    private UserPublicKey userPublicKey;
    private UserSecretKey userSecretKey;

    public UserKeyPair(UserPublicKey upk, UserSecretKey usk)
    {
        this.userPublicKey = upk;
        this.userSecretKey = usk;
    }
}
