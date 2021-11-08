package org.cryptimeleon.incentive.crypto.benchmark;

import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;

public class BenchmarkSpendDeductZkp {
    public static SpendDeductBooleanZkp getBenchmarkSpendDeductZkp(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey, Vector<BigInteger> subtractPoints) {
        Vector<BigInteger> ignore = Util.getNullBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> ones = Util.getOneBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> zeros = Util.getZeroBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> negatedSubtractPoints = Vector.fromStreamPlain(subtractPoints.stream().map(BigInteger::negate));

        // Range proof to ensure enough points + affine linear statements
        // Corresponds to the proofs from the paper but with vectors instead of single elements
        SpendDeductTree benchmarkTree = new TokenUpdateLeaf("TokenUpdateLeaf", zeros, ignore, ones, negatedSubtractPoints);

        return new SpendDeductBooleanZkp(benchmarkTree, pp, promotionParameters, providerPublicKey);
    }
}
