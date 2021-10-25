package org.cryptimeleon.incentive.crypto.benchmark;

import org.cryptimeleon.craco.protocols.arguments.sigma.SigmaProtocol;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.SpendDeductZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.TokenPointsRangeProof;
import org.cryptimeleon.incentive.crypto.proof.spend.TokenUpdateProof;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;

import java.math.BigInteger;
import java.util.Arrays;

public class BenchmarkSpendDeductZkp {
    public static SpendDeductZkp getBenchmarkSpendDeductZkp(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey, BigInteger[] subtractPoints) {
        BigInteger[] ignore = new BigInteger[promotionParameters.getPointsVectorSize()];
        Arrays.fill(ignore, null);

        BigInteger[] ones = new BigInteger[promotionParameters.getPointsVectorSize()];
        Arrays.fill(ones, BigInteger.ONE);


        SpendDeductTree conditionTree = new SpendDeductLeafNode() {
            @Override
            public SigmaProtocol getProtocol(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
                return new TokenPointsRangeProof(pp, subtractPoints, ignore, providerPublicKey, promotionParameters);
            }

            @Override
            public boolean isTrue() {
                return true;
            }

            @Override
            public String getLeafName() {
                return "ConditionNode";
            }
        };

        SpendDeductTree updateTree = new SpendDeductLeafNode() {
            @Override
            public SigmaProtocol getProtocol(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
                return new TokenUpdateProof(pp,
                        ignore,
                        ignore,
                        ones,
                        Arrays.stream(subtractPoints).map(BigInteger::negate).toArray(BigInteger[]::new),
                        providerPublicKey,
                        promotionParameters);
            }

            @Override
            public boolean isTrue() {
                return true;
            }

            @Override
            public String getLeafName() {
                return "UpdateNode";
            }
        };

        return new SpendDeductZkp(conditionTree, updateTree, pp, promotionParameters, providerPublicKey);
    }
}
