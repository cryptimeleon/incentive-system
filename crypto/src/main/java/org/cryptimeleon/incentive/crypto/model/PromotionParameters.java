package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.math.BigInteger;
import java.util.Objects;

/**
 * This class contains promotion-specific data.
 */
public class PromotionParameters implements StandaloneRepresentable {

    @Represented
    private BigInteger promotionId; // or some other fixed identifier

    @Represented
    private Integer pointsVectorSize;

    public PromotionParameters(Representation repr) {
        ReprUtil.deserialize(this, repr);
    }

    public PromotionParameters(BigInteger promotionId, Integer pointsVectorSize) {
        this.promotionId = promotionId;
        this.pointsVectorSize = pointsVectorSize;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    public BigInteger getPromotionId() {
        return this.promotionId;
    }

    public Integer getPointsVectorSize() {
        return this.pointsVectorSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromotionParameters that = (PromotionParameters) o;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(pointsVectorSize, that.pointsVectorSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, pointsVectorSize);
    }

    public String toString() {
        return "PromotionParameters(promotionId=" + this.getPromotionId() + ", pointsVectorSize=" + this.getPointsVectorSize() + ")";
    }
}
