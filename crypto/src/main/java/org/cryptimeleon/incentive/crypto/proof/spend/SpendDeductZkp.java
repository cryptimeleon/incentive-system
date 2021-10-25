package org.cryptimeleon.incentive.crypto.proof.spend;

import org.cryptimeleon.craco.protocols.arguments.sigma.SigmaProtocol;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;

public class SpendDeductZkp extends SpendDeductBooleanZkp {

    public SpendDeductZkp(SpendDeductTree spendDeductConditionTree, SpendDeductTree spendDeductUpdateTree, IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
        super(new SpendDeductAndNode(new SpendDeductAndNode(
                        spendDeductConditionTree,
                        spendDeductUpdateTree
                ),
                        new SpendDeductLeafNode() {
                            @Override
                            public SigmaProtocol getProtocol(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
                                return new MetadataZkp(pp, providerPublicKey, promotionParameters);
                            }

                            @Override
                            public boolean isTrue() {
                                return true;
                            }

                            @Override
                            public String getLeafName() {
                                return "MetadataNode";
                            }
                        }),
                pp, promotionParameters, providerPublicKey);
    }
}
