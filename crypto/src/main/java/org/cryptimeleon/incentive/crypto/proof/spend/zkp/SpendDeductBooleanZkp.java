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
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenPointsLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductAndNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductOrNode;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.expressions.bool.BooleanExpression;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.rings.RingElement;

/**
 * Proofs of partial knowledge that can be assembled from a SpendDeductTree.
 * All sub-protocols share a common input and witness space for simplicity.
 */
public class SpendDeductBooleanZkp extends ProofOfPartialKnowledge {

    private static final String META_LEAF_NAME = "MetadataLeaf";
    private final SpendDeductTree spendDeductTree;
    private final IncentivePublicParameters pp;
    private final PromotionParameters promotionParameters;
    private final ProviderPublicKey providerPublicKey;

    /**
     * Constructor.
     *
     * @param spendDeductTree     a SpendDeductTree that represents the ZKP
     * @param pp                  the public parameters
     * @param promotionParameters the promotion parameters
     * @param providerPublicKey   the public key of the provider
     */
    public SpendDeductBooleanZkp(SpendDeductTree spendDeductTree, IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
        this.spendDeductTree = spendDeductTree;
        this.pp = pp;
        this.promotionParameters = promotionParameters;
        this.providerPublicKey = providerPublicKey;
    }

    /**
     * Wrapper function around the recursive calls for generating the protocol tree.
     * Always inserts a metadata zkp to the protocol tree that must be true.
     */
    private ProtocolTree generateProtocolTree(CommonInput commonInput, SendFirstValue sendFirstValue) {
        return and(
                // Metadata proof must always be true
                leaf(META_LEAF_NAME, new MetadataZkp(this.pp, this.providerPublicKey, this.promotionParameters), commonInput),
                generateProtocolTree(this.spendDeductTree, commonInput, sendFirstValue)
        );
    }

    /**
     * Function used to recursively generate the protocol tree.
     */
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

    /**
     * Wrapper function around the recursive function with the same name for adding all known witnesses to the builder.
     */
    private void putWitnesses(CommonInput commonInput, SecretInput secretInput, ProverSpecBuilder builder) {
        // Metadata leaf must always be satisfied
        builder.putSecretInput(META_LEAF_NAME, secretInput);
        putWitnesses(this.spendDeductTree, commonInput, secretInput, builder);
    }

    /**
     * Recursively adds witness to all nodes that are valid the builder.
     */
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
            SpendDeductZkpWitnessInput witnessInput = (SpendDeductZkpWitnessInput) secretInput;
            if (spendDeductLeafNode.isValidForPoints(witnessInput.pointsVector.map(RingElement::asInteger), witnessInput.newPointsVector.map(RingElement::asInteger))) {
                builder.putSecretInput(spendDeductLeafNode.getLeafName(), secretInput);
            }
        } else {
            throw new RuntimeException("Unexpected instance of SpendDeductTree found!");
        }
    }

    /**
     * Helper function that creates the matching SigmaProtocol for a SpendDeductLeafNode.
     */
    private SigmaProtocol getProtocolForLeaf(SpendDeductLeafNode leafNode, IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey) {
        if (leafNode instanceof TokenPointsLeaf) {
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
