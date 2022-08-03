package org.cryptimeleon.incentive.crypto.model.keys.user;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.hash.annotations.AnnotatedUbrUtil;
import org.cryptimeleon.math.hash.annotations.UniqueByteRepresented;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
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

    @NonFinal
    @Represented(restorer = "spsEq")
    @UniqueByteRepresented
    SPSEQSignature genesisSignature;

    public UserSecretKey(ZnElement usk, PrfKey prfKey) {
        this.usk = usk;
        this.prfKey = prfKey;
    }

    public UserSecretKey(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getBg().getZn(), "Zn")
                .register(pp.getPrfToZn().getLongAesPseudoRandomFunction()::restoreKey, "longAes")
                .register(pp.getSpsEq(), "spsEq")
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
