package org.cryptimeleon.incentive.crypto.model.keys.provider;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQVerificationKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;

@Value
public class ProviderPublicKey implements Representable {
    @NonFinal
    @Represented(restorer = "SPSEQScheme")
    SPSEQVerificationKey pkSpsEq;

    @NonFinal
    @Represented(restorer = "G1")
    GroupElementVector h; // first six bases for the Pedersen commitment in the tokens

    public ProviderPublicKey(SPSEQVerificationKey pkSpsEq, GroupElementVector h) throws IllegalArgumentException {
        this.pkSpsEq = pkSpsEq;
        this.h = h;
    }

    public ProviderPublicKey(Representation repr, SPSEQSignatureScheme spseqSignatureScheme, Group group1) {
        new ReprUtil(this)
                .register(spseqSignatureScheme, "SPSEQScheme")
                .register(group1, "G1")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
