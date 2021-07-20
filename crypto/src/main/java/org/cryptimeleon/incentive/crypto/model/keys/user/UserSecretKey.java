package org.cryptimeleon.incentive.crypto.model.keys.user;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.prf.PrfKey;
import org.cryptimeleon.craco.prf.zn.HashThenPrfToZn;
import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.hash.annotations.AnnotatedUbrUtil;
import org.cryptimeleon.math.hash.annotations.UniqueByteRepresented;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

@Value
public class UserSecretKey implements Representable, UniqueByteRepresentable {
    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    ZnElement usk;

    @NonFinal
    @Represented(restorer = "longAes")
    @UniqueByteRepresented
    PrfKey prfKey; // user's key for generating pseudorandom ZnElements using the PRF

    public UserSecretKey(ZnElement usk, PrfKey prfKey) {
        this.usk = usk;
        this.prfKey = prfKey;
    }

    public UserSecretKey(Representation repr, Zn zn, HashThenPrfToZn hashThenPrfToZn) {
        new ReprUtil(this)
                .register(zn, "Zn")
                .register(hashThenPrfToZn.getLongAesPseudoRandomFunction()::restoreKey, "longAes")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return AnnotatedUbrUtil.autoAccumulate(accumulator, this);
    }
}
