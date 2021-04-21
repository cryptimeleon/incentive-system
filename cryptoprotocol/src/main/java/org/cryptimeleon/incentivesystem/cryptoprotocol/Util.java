package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Collection of utility functions
 */
public class Util {
    /**
     * Hash function to retrieve ZnElement gamma in spend-deduct
     *
     * @param zn    ZnElement to retrieve
     * @param k     earnAmount to hash
     * @param dsid  disid to hash
     * @param tid   tid to hash
     * @param cPre0 cPre0 to hash
     * @param cPre1 cPre1 to hash
     * @return hashed ZnElement gamma
     */
    public static Zn.ZnElement hashGamma(Zn zn, BigInteger k, GroupElement dsid, Zn.ZnElement tid, GroupElement cPre0, GroupElement cPre1) {
        var hashfunction = new HashIntoZn(zn);
        var accumulator = new ByteArrayAccumulator();
        accumulator.escapeAndAppend(k.toByteArray());
        accumulator.escapeAndAppend(dsid.getUniqueByteRepresentation());
        accumulator.escapeAndAppend(tid.getUniqueByteRepresentation());
        accumulator.escapeAndAppend(cPre0.getUniqueByteRepresentation());
        accumulator.escapeAndAppend(cPre1.getUniqueByteRepresentation());
        return hashfunction.hash(accumulator.extractBytes());
    }
}
