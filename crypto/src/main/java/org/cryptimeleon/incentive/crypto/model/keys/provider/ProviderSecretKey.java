package org.cryptimeleon.incentive.crypto.model.keys.provider;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.prf.PrfKey;
import org.cryptimeleon.craco.prf.zn.HashThenPrfToZn;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSigningKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

@Value
public class ProviderSecretKey implements Representable {
    @NonFinal
    @Represented(restorer = "SPSEQ")
    SPSEQSigningKey skSpsEq;

    @NonFinal
    @Represented(restorer = "Zn")
    RingElementVector q; // dlogs of the first six bases used in the Pedersen commitment in the token

    @NonFinal
    @Represented(restorer = "longAes")
    PrfKey betaProv; // Prf Key for PrfToZn

    public ProviderSecretKey(SPSEQSigningKey skSpsEq, RingElementVector q, PrfKey betaProv) throws IllegalArgumentException {
        // assert that correct number of Zn exponents is passed ()
        if (q.length() != 6) {
            throw new IllegalArgumentException("q is required to consist of 6 group elements, found: " + q.length());
        }

        this.skSpsEq = skSpsEq;
        this.q = q;
        this.betaProv = betaProv;
    }

    public ProviderSecretKey(Representation repr, SPSEQSignatureScheme spseqSignatureScheme, Zn zn, HashThenPrfToZn prfToZn) {
        new ReprUtil(this)
                .register(spseqSignatureScheme, "SPSEQ")
                .register(zn, "Zn")
                .register(prfToZn.getLongAesPseudoRandomFunction()::restoreKey, "longAes")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
