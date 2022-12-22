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

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserPreSecretKey)) return false;
        final UserPreSecretKey other = (UserPreSecretKey) o;
        final Object this$usk = this.getUsk();
        final Object other$usk = other.getUsk();
        if (!Objects.equals(this$usk, other$usk)) return false;
        final Object this$prfKey = this.getPrfKey();
        final Object other$prfKey = other.getPrfKey();
        return Objects.equals(this$prfKey, other$prfKey);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $usk = this.getUsk();
        result = result * PRIME + ($usk == null ? 43 : $usk.hashCode());
        final Object $prfKey = this.getPrfKey();
        result = result * PRIME + ($prfKey == null ? 43 : $prfKey.hashCode());
        return result;
    }

    public String toString() {
        return "UserPreSecretKey(usk=" + this.getUsk() + ", prfKey=" + this.getPrfKey() + ")";
    }
}
