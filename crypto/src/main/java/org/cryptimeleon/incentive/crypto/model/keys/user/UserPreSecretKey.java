package org.cryptimeleon.incentive.crypto.model.keys.user;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.hash.annotations.UniqueByteRepresented;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Objects;

public class UserPreSecretKey implements Representable {
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    private
    Zn.ZnElement usk;

    @Represented(restorer = "longAes")
    @UniqueByteRepresented
    private
    PrfKey prfKey; // user's key for generating pseudorandom ZnElements using the PRF

    public UserPreSecretKey(Zn.ZnElement usk, PrfKey prfKey) {
        this.usk = usk;
        this.prfKey = prfKey;
    }

    public UserPreSecretKey(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getBg().getZn(), "Zn")
                .register(pp.getPrfToZn().getLongAesPseudoRandomFunction()::restoreKey, "longAes")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public Zn.ZnElement getUsk() {
        return this.usk;
    }

    public PrfKey getPrfKey() {
        return this.prfKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreSecretKey that = (UserPreSecretKey) o;
        return Objects.equals(usk, that.usk) && Objects.equals(prfKey, that.prfKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usk, prfKey);
    }

    public String toString() {
        return "UserPreSecretKey(usk=" + this.getUsk() + ", prfKey=" + this.getPrfKey() + ")";
    }
}
