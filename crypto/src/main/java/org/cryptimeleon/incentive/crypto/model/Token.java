package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Data holding class representing a token from a mathematical point of view (meaning: as a bunch of group elements and exponents).
 */
public class Token implements Representable, UniqueByteRepresentable {

    @Represented(restorer = "G1")
    private GroupElement commitment0; // the first part of the Pedersen commitment computed from the bases and the exponents, representing the actual token

    @Represented(restorer = "G1")
    private GroupElement commitment1; // the second part of the Pedersen commitment computed from the bases and the exponents, representing the actual token

    @Represented(restorer = "Zn")
    private ZnElement doubleSpendRandomness0; // randomness used for the first challenge generation in double spending protection

    @Represented(restorer = "Zn")
    private ZnElement doubleSpendRandomness1; // randomness used for the second challenge generation in double spending protection

    @Represented(restorer = "Zn")
    private ZnElement z; // first value for blinding the token group element

    @Represented(restorer = "Zn")
    private ZnElement t; // second value for blinding the token group element (needed for sophisticated proof reasons)

    @Represented
    private BigInteger promotionId;

    @Represented(restorer = "Zn")
    private RingElementVector points; // number of points that the token currently stores (initially 0), v in the 2020 paper

    @Represented(restorer = "SPSEQ")
    private SPSEQSignature signature; // the SPS-EQ certifying the commitment as well-formed and valid

    public Token(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getBg().getZn(), "Zn")
                .register(pp.getBg().getG1(), "G1")
                .register(pp.getSpsEq(), "SPSEQ")
                .deserialize(repr);
    }

    public Token(GroupElement commitment0, GroupElement commitment1, ZnElement doubleSpendRandomness0, ZnElement doubleSpendRandomness1, ZnElement z, ZnElement t, BigInteger promotionId, RingElementVector points, SPSEQSignature signature) {
        this.commitment0 = commitment0;
        this.commitment1 = commitment1;
        this.doubleSpendRandomness0 = doubleSpendRandomness0;
        this.doubleSpendRandomness1 = doubleSpendRandomness1;
        this.z = z;
        this.t = t;
        this.promotionId = promotionId;
        this.points = points;
        this.signature = signature;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        points.stream().forEachOrdered(k -> accumulator.escapeAndSeparate(k.getUniqueByteRepresentation()));
        accumulator.escapeAndSeparate(this.commitment0.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(this.commitment1.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(this.doubleSpendRandomness0.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(this.doubleSpendRandomness1.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(this.signature.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(this.promotionId.toByteArray());
        accumulator.escapeAndSeparate(this.z.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(this.t.getUniqueByteRepresentation());
        return accumulator;
    }

    /**
     * Computes the double-spending ID for this token as defined in the 2020 incentive system paper.
     *
     * @param pp public parameters of the incentive system this token is used in
     * @return element of group G1
     */
    public GroupElement computeDsid(IncentivePublicParameters pp) {
        return pp.getW().pow(0);
    }

    public GroupElement getCommitment0() {
        return this.commitment0;
    }

    public GroupElement getCommitment1() {
        return this.commitment1;
    }

    public ZnElement getDoubleSpendRandomness0() {
        return this.doubleSpendRandomness0;
    }

    public ZnElement getDoubleSpendRandomness1() {
        return this.doubleSpendRandomness1;
    }

    public ZnElement getZ() {
        return this.z;
    }

    public ZnElement getT() {
        return this.t;
    }

    public BigInteger getPromotionId() {
        return this.promotionId;
    }

    public RingElementVector getPoints() {
        return this.points;
    }

    public SPSEQSignature getSignature() {
        return this.signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(commitment0, token.commitment0) && Objects.equals(commitment1, token.commitment1) && Objects.equals(doubleSpendRandomness0, token.doubleSpendRandomness0) && Objects.equals(doubleSpendRandomness1, token.doubleSpendRandomness1) && Objects.equals(z, token.z) && Objects.equals(t, token.t) && Objects.equals(promotionId, token.promotionId) && Objects.equals(points, token.points) && Objects.equals(signature, token.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commitment0, commitment1, doubleSpendRandomness0, doubleSpendRandomness1, z, t, promotionId, points, signature);
    }

    public String toString() {
        return "Token(commitment0=" + this.getCommitment0() + ", commitment1=" + this.getCommitment1() + ", doubleSpendRandomness0=" + this.getDoubleSpendRandomness0() + ", doubleSpendRandomness1=" + this.getDoubleSpendRandomness1() + ", z=" + this.getZ() + ", t=" + this.getT() + ", promotionId=" + this.getPromotionId() + ", points=" + this.getPoints() + ", signature=" + this.getSignature() + ")";
    }
}
