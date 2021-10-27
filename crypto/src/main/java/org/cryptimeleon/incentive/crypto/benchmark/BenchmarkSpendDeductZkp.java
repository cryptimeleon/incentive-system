package org.cryptimeleon.incentive.crypto.benchmark;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkp;

import java.math.BigInteger;
import java.util.Arrays;

public class BenchmarkSpendDeductZkp {
    public static SpendDeductZkp getBenchmarkSpendDeductZkp(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey, BigInteger[] subtractPoints) {
        BigInteger[] ignore = new BigInteger[promotionParameters.getPointsVectorSize()];
        Arrays.fill(ignore, null);

        BigInteger[] ones = new BigInteger[promotionParameters.getPointsVectorSize()];
        Arrays.fill(ones, BigInteger.ONE);


        SpendDeductTree conditionTree = new TokenPointsLeaf("TokenPointsLeaf", subtractPoints, ignore, true);
        SpendDeductTree updateTree = new TokenUpdateLeaf("TokenUpdateLeaf", ignore, ignore, ones, Arrays.stream(subtractPoints).map(BigInteger::negate).toArray(BigInteger[]::new), true);

        return new SpendDeductZkp(conditionTree, updateTree, pp, promotionParameters, providerPublicKey);
    }
}
