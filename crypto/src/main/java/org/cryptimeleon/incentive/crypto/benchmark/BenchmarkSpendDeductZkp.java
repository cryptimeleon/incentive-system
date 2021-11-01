package org.cryptimeleon.incentive.crypto.benchmark;

import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkp;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;

public class BenchmarkSpendDeductZkp {
    public static SpendDeductZkp getBenchmarkSpendDeductZkp(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey, Vector<BigInteger> subtractPoints) {
        Vector<BigInteger> ignore = Util.getNullBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> ones = Util.getOneBigIntegerVector(promotionParameters.getPointsVectorSize());
        Vector<BigInteger> negatedSubtractPoints = Vector.fromStreamPlain(subtractPoints.stream().map(BigInteger::negate));

        SpendDeductTree conditionTree = new TokenPointsLeaf("TokenPointsLeaf", subtractPoints, ignore, true);
        SpendDeductTree updateTree = new TokenUpdateLeaf("TokenUpdateLeaf", ignore, ignore, ones, negatedSubtractPoints, true);

        return new SpendDeductZkp(conditionTree, updateTree, pp, promotionParameters, providerPublicKey);
    }
}
