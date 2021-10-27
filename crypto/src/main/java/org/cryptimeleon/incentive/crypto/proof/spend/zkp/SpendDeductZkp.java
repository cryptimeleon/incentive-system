package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.MetadataLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;

public class SpendDeductZkp extends SpendDeductBooleanZkp {

    public SpendDeductZkp(SpendDeductTree spendDeductConditionTree, SpendDeductTree spendDeductUpdateTree, IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
        super(
                new SpendDeductAndNode(
                        new SpendDeductAndNode(
                                spendDeductConditionTree,
                                spendDeductUpdateTree
                        ),
                        new MetadataLeaf()
                ),
                pp,
                promotionParameters,
                providerPublicKey
        );
    }
}
