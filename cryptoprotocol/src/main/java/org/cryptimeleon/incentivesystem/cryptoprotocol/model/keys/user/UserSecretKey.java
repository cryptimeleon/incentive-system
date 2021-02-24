package org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.prf.PrfKey;
import org.cryptimeleon.craco.prf.aes.AesPseudorandomFunction;
import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

@Value
public class UserSecretKey implements Representable {
    @NonFinal
    @Represented
    ZnElement usk;

    @NonFinal
    @Represented
    PrfKey prfKey; // user's key for generating pseudorandomness using the PRF

    public UserSecretKey(ZnElement usk, PrfKey prfKey) {
        this.usk = usk;
        this.prfKey = prfKey;
    }

    public UserSecretKey(Representation repr, Zn zn, AesPseudorandomFunction aes) {
        ObjectRepresentation objectRepresentation = repr.obj();
        usk = zn.getElement(objectRepresentation.get("usk"));
        prfKey = aes.getKey(objectRepresentation.get("prfKey"));
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
