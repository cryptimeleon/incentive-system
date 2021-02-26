package org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.prf.PrfKey;
import org.cryptimeleon.craco.prf.aes.AesPseudorandomFunction;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

@Value
public class UserSecretKey implements Representable {
    @NonFinal
    @Represented(restorer = "Zn")
    ZnElement usk;

    @NonFinal
    @Represented(restorer = "aes")
    PrfKey prfKey; // user's key for generating pseudorandomness using the PRF

    public UserSecretKey(ZnElement usk, PrfKey prfKey) {
        this.usk = usk;
        this.prfKey = prfKey;
    }

    public UserSecretKey(Representation repr, Zn zn, AesPseudorandomFunction aes) {
        new ReprUtil(this)
                .register(zn, "Zn")
                .register(aes::restoreKey, "aes")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
