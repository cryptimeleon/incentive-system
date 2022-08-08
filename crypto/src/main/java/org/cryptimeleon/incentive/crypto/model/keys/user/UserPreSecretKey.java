package org.cryptimeleon.incentive.crypto.model.keys.user;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.hash.annotations.UniqueByteRepresented;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

@Value
public class UserPreSecretKey implements Representable {
    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    Zn.ZnElement usk;

    @NonFinal
    @Represented(restorer = "longAes")
    @UniqueByteRepresented
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
}
