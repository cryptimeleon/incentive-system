package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;


/**
 * Collection of utility functions
 */
public class Util {
    public static UserKeyPair addRegistrationSignatureToUserPreKeys(UserPreKeyPair userPreKeyPair, ProviderKeyPair providerKeyPair, IncentivePublicParameters pp) {
        return new UserKeyPair(
                userPreKeyPair.getPk(),
                new UserSecretKey(
                        userPreKeyPair.getPsk().getUsk(),
                        userPreKeyPair.getPsk().getPrfKey(),
                        (SPSEQSignature) pp.getSpsEq().sign(
                            providerKeyPair.getSk().getRegistrationSpsEqSk(),
                            userPreKeyPair.getPk().getUpk(),
                            pp.getW()
                        )
                )
        );
    }

    /**
     * Hash function to retrieve ZnElement gamma in spend-deduct
     *
     * @param zn    ZnElement to retrieve
     * @param dsid  dsid to hash
     * @param tid   tid to hash
     * @param cPre0 cPre0 to hash
     * @param cPre1 cPre1 to hash
     * @return hashed ZnElement gamma
     */
    public static Zn.ZnElement hashGamma(Zn zn, GroupElement dsid, Zn.ZnElement tid, GroupElement cPre0, GroupElement cPre1, UniqueByteRepresentable userChoice) {
        var hashfunction = new HashIntoZn(zn);
        var accumulator = new ByteArrayAccumulator();
        accumulator.escapeAndSeparate(dsid.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(tid.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(cPre0.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(cPre1.getUniqueByteRepresentation());
        accumulator.escapeAndSeparate(userChoice);
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
    public static boolean arePointsInRange(Vector<BigInteger> pointsVector, Vector<BigInteger> lowerLimits, Vector<BigInteger> upperLimits) {
        boolean isValid = lowerLimits.zipReduce(pointsVector, (lowerLimit, points) -> lowerLimit == null || lowerLimit.compareTo(points) <= 0, (a, b) -> a && b);
        isValid &= upperLimits.zipReduce(pointsVector, (upperLimit, points) -> upperLimit == null || upperLimit.compareTo(points) >= 0, (a, b) -> a && b);
        return isValid;
    }

    public static boolean satisfyAffineLinearRelation(Vector<BigInteger> oldPointsVector,
                                                      Vector<BigInteger> newPointsVector,
                                                      Vector<BigInteger> aVector,
                                                      Vector<BigInteger> bVector) {
        boolean isValid = true;
        for (int i = 0; i < oldPointsVector.length(); i++) {
            if (aVector.get(i) != null && bVector.get(i) != null) {
                isValid &= newPointsVector.get(i).equals(aVector.get(i).multiply(oldPointsVector.get(i)).add(bVector.get(i)));
            }
        }
        return isValid;
    }
}
