package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn;

@Value
@AllArgsConstructor
public class SpendResponse implements Representable {
    @NonFinal
    @Represented(restorer = "SPSEQ")
    SPSEQSignature sigma;

    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement eskProvStar;

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public SpendResponse(Representation repr, Zn zn, SPSEQSignatureScheme spseqSignatureScheme) {
        new ReprUtil(this).register(zn, "Zn").register(spseqSignatureScheme, "SPSEQ").deserialize(repr);
    }
}
