package org.cryptimeleon.incentive.crypto.model.keys.provider;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQVerificationKey;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
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

    /**
     * Returns the full vector H of group elements which form the base for commitments. Length depends on promotion
     * parameters, since tokens can store a different number of points.
     *
     * @param publicParameters    the public parameters
     * @param promotionParameters the promotion for which we want the vector H
     * @return vector H
     */
    public GroupElementVector getH(IncentivePublicParameters publicParameters, PromotionParameters promotionParameters) {
        return getH().prepend(publicParameters.getH7()).truncate(Setup.H_SIZE_WITHOUT_POINTS + promotionParameters.getPointsVectorSize());
    }

    /**
     * Returns the part of vector H that is the base to the metadata in the commitment, namely to every exponent which
     * does not store the points vector in the token. Hence, this does not depend on the promotion.
     *
     * @param publicParameters the public parameters
     * @return metadata part of vector H
     */
    public GroupElementVector getTokenMetadataH(IncentivePublicParameters publicParameters) {
        return getH().prepend(publicParameters.getH7()).truncate(Setup.H_SIZE_WITHOUT_POINTS);
    }

    /**
     * Returns the part of vector H that is the base to the points vector of the token. The size of this vector and
     * thus the base depends on the promotion.
     *
     * @param promotionParameters the promotion parameters
     * @return vector H
     */
    public GroupElementVector getTokenPointsH(PromotionParameters promotionParameters) {
        // the -1 is because we do not count h7 from the paper
        return GroupElementVector.fromStream(getH().stream().skip(Setup.H_SIZE_WITHOUT_POINTS - 1)).truncate(promotionParameters.getPointsVectorSize());
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }
}
