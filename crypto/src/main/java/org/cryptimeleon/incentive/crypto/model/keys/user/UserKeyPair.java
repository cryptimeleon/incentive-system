package org.cryptimeleon.incentive.crypto.model.keys.user;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;

import java.util.Objects;

public class UserKeyPair {
    private final UserPublicKey pk;
    private final UserSecretKey sk;

    public UserKeyPair(UserPreKeyPair userPreKeyPair, SPSEQSignature genesisSignature) {
        pk = userPreKeyPair.getPk();
        sk = new UserSecretKey(userPreKeyPair.getPsk(), genesisSignature);
    }

    public UserKeyPair(UserPublicKey pk, UserSecretKey sk) {
        this.pk = pk;
        this.sk = sk;
    }

    public UserPublicKey getPk() {
        return this.pk;
    }

    public UserSecretKey getSk() {
        return this.sk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserKeyPair that = (UserKeyPair) o;
        return Objects.equals(pk, that.pk) && Objects.equals(sk, that.sk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pk, sk);
    }

    public String toString() {
        return "UserKeyPair(pk=" + this.getPk() + ", sk=" + this.getSk() + ")";
    }
}
