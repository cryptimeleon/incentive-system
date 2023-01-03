package org.cryptimeleon.incentive.crypto.model.keys.user;

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

import java.util.Objects;

public class UserSecretKey implements Representable, UniqueByteRepresentable {
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    private ZnElement usk;

    @Represented(restorer = "longAes")
    @UniqueByteRepresented
    private PrfKey prfKey; // user's key for generating pseudorandom ZnElements using the PRF

    @Represented(restorer = "spsEq")
    @UniqueByteRepresented
    private SPSEQSignature genesisSignature;

    public UserSecretKey(ZnElement usk, PrfKey prfKey, SPSEQSignature genesisSignature) {
        this.usk = usk;
        this.prfKey = prfKey;
        this.genesisSignature = genesisSignature;
    }

    public UserSecretKey(UserPreSecretKey userPreSecretKey, SPSEQSignature genesisSignature) {
        this.usk = userPreSecretKey.getUsk();
        this.prfKey = userPreSecretKey.getPrfKey();
        this.genesisSignature = genesisSignature;
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

    public ZnElement getUsk() {
        return this.usk;
    }

    public PrfKey getPrfKey() {
        return this.prfKey;
    }

    public SPSEQSignature getGenesisSignature() {
        return this.genesisSignature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSecretKey that = (UserSecretKey) o;
        return Objects.equals(usk, that.usk) && Objects.equals(prfKey, that.prfKey) && Objects.equals(genesisSignature, that.genesisSignature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usk, prfKey, genesisSignature);
    }

    public String toString() {
        return "UserSecretKey(usk=" + this.getUsk() + ", prfKey=" + this.getPrfKey() + ", genesisSignature=" + this.getGenesisSignature() + ")";
    }
}
