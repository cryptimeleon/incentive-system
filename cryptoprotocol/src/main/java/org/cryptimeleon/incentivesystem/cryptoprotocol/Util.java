package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.math.hash.impl.SHAHashAccumulator;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

/**
 * Collection of utility functions
 */
public class Util {
    /**
     * Hash function to retrieve gamma in spend-deduct
     * @param zn
     * @param k
     * @param dsid
     * @param tid
     * @param cPre0
     * @param cPre1
     * @return hashed ZnElement
     */
    public static Zn.ZnElement hashGamma(Zn zn, BigInteger k, GroupElement dsid, Zn.ZnElement tid, GroupElement cPre0, GroupElement cPre1) {
        var hashfunction = new HashIntoZn(zn);
        var accumulator = new SHAHashAccumulator("SHA-512");
        accumulator.append(zn.valueOf(k));
        accumulator.append(dsid);
        accumulator.append(tid);
        accumulator.append(cPre0);
        accumulator.append(cPre1);
        return hashfunction.hash(accumulator.extractBytes());
    }

}
