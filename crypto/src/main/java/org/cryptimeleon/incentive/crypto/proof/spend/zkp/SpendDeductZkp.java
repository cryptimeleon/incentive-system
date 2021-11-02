package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;

/**
 * A wrapper class that helps building typical SpendDeduct ZKPs that all hvae a Metadata leaf and a protocol tree for
 * conditions and updates each.
 */
public class SpendDeductZkp extends SpendDeductBooleanZkp {

    /**
     * Constructor.
     *
     * @param spendDeductConditionTree a SpendDeductTree that represents conditions on the old token which must be fulfilled
     * @param spendDeductUpdateTree    a SpendDeductTree that represents conditions on the new token relative to the old token
     * @param pp                       the public parameters
     * @param promotionParameters      the promotion parameters
     * @param providerPublicKey        the public key of the provider
     */
    public SpendDeductZkp(SpendDeductTree spendDeductConditionTree, SpendDeductTree spendDeductUpdateTree, IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
        super(
                new SpendDeductAndNode(
                        spendDeductConditionTree,
                        spendDeductUpdateTree
                ),
                pp,
                promotionParameters,
                providerPublicKey
        );
    }
}
