package org.cryptimeleon.incentive.crypto.proof.spend.zkp;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.ChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.SigmaProtocol;
import org.cryptimeleon.craco.protocols.arguments.sigma.ZnChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.partial.ProofOfPartialKnowledge;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendFirstValue;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.MetadataLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductOrNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.expressions.bool.BooleanExpression;
import org.cryptimeleon.math.serialization.Representation;

public class SpendDeductBooleanZkp extends ProofOfPartialKnowledge {

    private final SpendDeductTree spendDeductTree;
    private final IncentivePublicParameters pp;
    private final PromotionParameters promotionParameters;
    private final ProviderPublicKey providerPublicKey;

    public SpendDeductBooleanZkp(SpendDeductTree spendDeductTree, IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
        this.spendDeductTree = spendDeductTree;
        this.pp = pp;
        this.promotionParameters = promotionParameters;
        this.providerPublicKey = providerPublicKey;
    }

    private ProtocolTree generateProtocolTree(CommonInput commonInput, SendFirstValue sendFirstValue) {
        return generateProtocolTree(this.spendDeductTree, commonInput, sendFirstValue);
    }

    private ProtocolTree generateProtocolTree(SpendDeductTree spendDeductTree, CommonInput commonInput, SendFirstValue sendFirstValue) {
        if (spendDeductTree instanceof SpendDeductOrNode) {
            SpendDeductOrNode spendDeductOrNode = (SpendDeductOrNode) spendDeductTree;
            return or(
                    generateProtocolTree(spendDeductOrNode.left, commonInput, sendFirstValue),
                    generateProtocolTree(spendDeductOrNode.right, commonInput, sendFirstValue)
            );
        } else if (spendDeductTree instanceof SpendDeductAndNode) {
            SpendDeductAndNode spendDeductAndNode = (SpendDeductAndNode) spendDeductTree;
            return and(
                    generateProtocolTree(spendDeductAndNode.left, commonInput, sendFirstValue),
                    generateProtocolTree(spendDeductAndNode.right, commonInput, sendFirstValue)
            );
        } else if (spendDeductTree instanceof SpendDeductLeafNode) {
            SpendDeductLeafNode spendDeductLeafNode = (SpendDeductLeafNode) spendDeductTree;
            return leaf(
                    spendDeductLeafNode.getLeafName(),
                    getProtocolForLeaf(spendDeductLeafNode, pp, promotionParameters, providerPublicKey),
                    commonInput
            );
        } else {
            throw new RuntimeException("Unexpected instance of SpendDeductTree found!");
        }
    }

    private void putWitnesses(CommonInput commonInput, SecretInput secretInput, ProverSpecBuilder builder) {
        putWitnesses(this.spendDeductTree, commonInput, secretInput, builder);
    }

    private void putWitnesses(SpendDeductTree spendDeductTree, CommonInput commonInput, SecretInput secretInput, ProverSpecBuilder builder) {
        if (spendDeductTree instanceof SpendDeductOrNode) {
            SpendDeductOrNode spendDeductOrNode = (SpendDeductOrNode) spendDeductTree;
            putWitnesses(spendDeductOrNode.left, commonInput, secretInput, builder);
            putWitnesses(spendDeductOrNode.right, commonInput, secretInput, builder);
        } else if (spendDeductTree instanceof SpendDeductAndNode) {
            SpendDeductAndNode spendDeductAndNode = (SpendDeductAndNode) spendDeductTree;
            putWitnesses(spendDeductAndNode.left, commonInput, secretInput, builder);
            putWitnesses(spendDeductAndNode.right, commonInput, secretInput, builder);
        } else if (spendDeductTree instanceof SpendDeductLeafNode) {
            SpendDeductLeafNode spendDeductLeafNode = (SpendDeductLeafNode) spendDeductTree;
            if (spendDeductLeafNode.hasWitness()) {
                builder.putSecretInput(spendDeductLeafNode.getLeafName(), secretInput);
            }
        } else {
            throw new RuntimeException("Unexpected instance of SpendDeductTree found!");
        }
    }

    private SigmaProtocol getProtocolForLeaf(SpendDeductLeafNode leafNode, IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
        if (leafNode instanceof MetadataLeaf) {
            MetadataLeaf l = (MetadataLeaf) leafNode;
            return new MetadataZkp(pp, providerPublicKey, promotionParameters);
        } else if (leafNode instanceof TokenPointsLeaf) {
            TokenPointsLeaf l = (TokenPointsLeaf) leafNode;
            return new TokenPointsZkp(pp, l.lowerLimits, l.upperLimits, providerPublicKey, promotionParameters);
        } else if (leafNode instanceof TokenUpdateLeaf) {
            TokenUpdateLeaf l = (TokenUpdateLeaf) leafNode;
            return new TokenUpdateZkp(pp, l.lowerLimits, l.upperLimits, l.aVector, l.bVector, providerPublicKey, promotionParameters);
        } else {
            throw new RuntimeException("Unexpected instance of TokenPointsRangeProofLeaf found!");
        }
    }

    @Override
    protected ProtocolTree provideProtocolTree(CommonInput commonInput, SendFirstValue sendFirstValue) {
        return generateProtocolTree(commonInput, sendFirstValue);
    }

    @Override
    protected ProverSpec provideProverSpec(CommonInput commonInput, SecretInput secretInput, ProverSpecBuilder builder) {
        builder.setSendFirstValue(SendFirstValue.EMPTY);
        putWitnesses(commonInput, secretInput, builder);
        return builder.build();
    }

    @Override
    protected SendFirstValue restoreSendFirstValue(CommonInput commonInput, Representation repr) {
        return SendFirstValue.EMPTY;
    }

    @Override
    protected SendFirstValue simulateSendFirstValue(CommonInput commonInput) {
        return SendFirstValue.EMPTY;
    }

    @Override
    protected BooleanExpression provideAdditionalCheck(CommonInput commonInput, SendFirstValue sendFirstValue) {
        return BooleanExpression.TRUE;
    }

    @Override
    public ChallengeSpace getChallengeSpace(CommonInput commonInput) {
        return new ZnChallengeSpace(this.pp.getBg().getZn());
    }
}
