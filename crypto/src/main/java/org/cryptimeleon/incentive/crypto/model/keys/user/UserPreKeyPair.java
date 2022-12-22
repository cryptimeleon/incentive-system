package org.cryptimeleon.incentive.crypto.model.keys.user;

import java.util.Objects;

public class UserPreKeyPair {
    private final UserPublicKey pk;
    private final UserPreSecretKey psk;

    public UserPreKeyPair(UserPublicKey pk, UserPreSecretKey psk) {
        this.pk = pk;
        this.psk = psk;
    }

    public UserPublicKey getPk() {
        return this.pk;
    }

    public UserPreSecretKey getPsk() {
        return this.psk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreKeyPair that = (UserPreKeyPair) o;
        return Objects.equals(pk, that.pk) && Objects.equals(psk, that.psk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pk, psk);
    }

    public String toString() {
        return "UserPreKeyPair(pk=" + this.getPk() + ", psk=" + this.getPsk() + ")";
    }
}
