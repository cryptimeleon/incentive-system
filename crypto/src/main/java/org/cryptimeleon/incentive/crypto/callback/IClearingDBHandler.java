package org.cryptimeleon.incentive.crypto.callback;

import org.cryptimeleon.incentive.crypto.model.EarnRequestECDSA;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;

public interface IClearingDBHandler {
    void addEarningDataToClearingDB(BigInteger promotionId, Vector<BigInteger> deltaK, byte[] h, EarnRequestECDSA earnRequestECDSA);
}
