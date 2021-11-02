package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;


/**
 * Collection of utility functions
 */
public class Util {

    /**
     * Hash function to retrieve ZnElement gamma in spend-deduct
     * TODO think about putting additional input to replace k
     * TODO no, we have the new commitments in the hash
     *
     * @param zn    ZnElement to retrieve
     * @param dsid  dsid to hash
     * @param tid   tid to hash
     * @param cPre0 cPre0 to hash
     * @param cPre1 cPre1 to hash
     * @return hashed ZnElement gamma
     */
    public static Zn.ZnElement hashGamma(Zn zn, GroupElement dsid, Zn.ZnElement tid, GroupElement cPre0, GroupElement cPre1) {
        var hashfunction = new HashIntoZn(zn);
        var accumulator = new ByteArrayAccumulator();
        accumulator.escapeAndSeparate(dsid.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(tid.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(cPre0.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(cPre1.getUniqueByteRepresentation());
        return hashfunction.hash(accumulator.extractBytes());
    }

    /**
     * Returns a Vector of size many null elements.
     *
     * @param size size of the vector
     * @return vector of type BigInteger
     */
    public static Vector<BigInteger> getNullBigIntegerVector(int size) {
        return Vector.iterate(null, e -> e, size);
    }

    /**
     * Returns a Vector of size many zero elements.
     *
     * @param size size of the vector
     * @return vector of type BigInteger
     */
    public static Vector<BigInteger> getZeroBigIntegerVector(int size) {
        return Vector.iterate(BigInteger.ZERO, e -> e, size);
    }

    /**
     * Returns a Vector of size many one elements.
     *
     * @param size size of the vector
     * @return vector of type BigInteger
     */
    public static Vector<BigInteger> getOneBigIntegerVector(int size) {
        return Vector.iterate(BigInteger.ONE, e -> e, size);
    }

    /**
     * Determines whether {@literal lowerLimits[i] <= pointsVector[i] <= upperLimits[i] for all i}.
     * If limits are null, relation skipped.
     *
     * @param pointsVector a vector of points
     * @param lowerLimits  a vector of lower limits
     * @param upperLimits  a vector of upper limits
     * @return whether are relations are satisfied
     */
    public static boolean arePointsInRange(RingElementVector pointsVector, Vector<BigInteger> lowerLimits, Vector<BigInteger> upperLimits) {
        boolean isValid = lowerLimits.zipReduce(pointsVector, (lowerLimit, points) -> lowerLimit == null || lowerLimit.compareTo(points.asInteger()) <= 0, (a, b) -> a && b);
        isValid &= upperLimits.zipReduce(pointsVector, (upperLimit, points) -> upperLimit == null || upperLimit.compareTo(points.asInteger()) >= 0, (a, b) -> a && b);
        return isValid;
    }
}
