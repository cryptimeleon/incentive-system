package org.cryptimeleon.incentive.crypto.model.keys.provider;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSigningKey;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.prf.zn.HashThenPrfToZn;
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

    public ProviderSecretKey(SPSEQSigningKey skSpsEq, RingElementVector q, PrfKey betaProv) {
        this.skSpsEq = skSpsEq;
        this.q = q;
        this.betaProv = betaProv;
    }

    @Deprecated
    public ProviderSecretKey(Representation repr, SPSEQSignatureScheme spseqSignatureScheme, Zn zn, HashThenPrfToZn prfToZn) {
        new ReprUtil(this)
                .register(spseqSignatureScheme, "SPSEQ")
                .register(zn, "Zn")
                .register(prfToZn.getLongAesPseudoRandomFunction()::restoreKey, "longAes")
                .deserialize(repr);
    }

    public ProviderSecretKey(Representation repr, IncentivePublicParameters pp) {
        this(repr, pp.getSpsEq(), pp.getBg().getZn(), pp.getPrfToZn());
    }

    /**
     * Returns the DLOGs (Q in the paper) of the H vector that is used to store the points vector in the token. Depends on the promotion
     * parameters since the vector size can vary between promotions.
     *
     * @param promotionParameters the promotion parameters
     * @return the vector of DLOGS
     */
    public RingElementVector getTokenPointsQ(PromotionParameters promotionParameters) {
        // the -1 is because we do not know the DLOG q7 to h7 from the paper
        return RingElementVector.fromStream(getQ().stream().skip(Setup.H_SIZE_WITHOUT_POINTS - 1)).truncate(promotionParameters.getPointsVectorSize());
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
