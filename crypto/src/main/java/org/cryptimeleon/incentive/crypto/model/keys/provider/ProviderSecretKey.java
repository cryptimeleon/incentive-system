package org.cryptimeleon.incentive.crypto.model.keys.provider;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSigningKey;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

import java.util.Objects;

public class ProviderSecretKey implements Representable {
    @Represented(restorer = "SPSEQ")
    private SPSEQSigningKey skSpsEq;

    @Represented(restorer = "SPSEQ")
    private SPSEQSigningKey genesisSpsEqSk;

    @Represented(restorer = "Zn")
    private RingElementVector q; // dlogs of the first six bases used in the Pedersen commitment in the token

    @Represented(restorer = "longAes")
    private PrfKey betaProv; // Prf Key for PrfToZn

    public ProviderSecretKey(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getSpsEq(), "SPSEQ")
                .register(pp.getBg().getZn(), "Zn")
                .register(pp.getPrfToZn().getLongAesPseudoRandomFunction()::restoreKey, "longAes")
                .deserialize(repr);
    }

    public ProviderSecretKey(SPSEQSigningKey skSpsEq, SPSEQSigningKey genesisSpsEqSk, RingElementVector q, PrfKey betaProv) {
        this.skSpsEq = skSpsEq;
        this.genesisSpsEqSk = genesisSpsEqSk;
        this.q = q;
        this.betaProv = betaProv;
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

    public SPSEQSigningKey getSkSpsEq() {
        return this.skSpsEq;
    }

    public SPSEQSigningKey getGenesisSpsEqSk() {
        return this.genesisSpsEqSk;
    }

    public RingElementVector getQ() {
        return this.q;
    }

    public PrfKey getBetaProv() {
        return this.betaProv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderSecretKey that = (ProviderSecretKey) o;
        return Objects.equals(skSpsEq, that.skSpsEq) && Objects.equals(genesisSpsEqSk, that.genesisSpsEqSk) && Objects.equals(q, that.q) && Objects.equals(betaProv, that.betaProv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skSpsEq, genesisSpsEqSk, q, betaProv);
    }

    public String toString() {
        return "ProviderSecretKey(skSpsEq=" + this.getSkSpsEq() + ", genesisSpsEqSk=" + this.getGenesisSpsEqSk() + ", q=" + this.getQ() + ", betaProv=" + this.getBetaProv() + ")";
    }
}
