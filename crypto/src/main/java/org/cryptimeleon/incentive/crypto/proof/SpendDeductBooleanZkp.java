package org.cryptimeleon.incentive.crypto.proof;

import org.cryptimeleon.craco.protocols.CommonInput;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.ChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.ZnChallengeSpace;
import org.cryptimeleon.craco.protocols.arguments.sigma.partial.ProofOfPartialKnowledge;
import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.SendFirstValue;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.expressions.bool.BooleanExpression;
import org.cryptimeleon.math.serialization.Representation;

public class SpendDeductBooleanZkp extends ProofOfPartialKnowledge {

    private SpendDeductTree spendDeductTree;
    private IncentivePublicParameters pp;

    public SpendDeductBooleanZkp(SpendDeductTree spendDeductTree, IncentivePublicParameters pp) {
        this.spendDeductTree = spendDeductTree;
        this.pp = pp;
    }

    private ProtocolTree generateProtocolTree(CommonInput commonInput, SendFirstValue sendFirstValue) {
        return generateProtocolTree(this.spendDeductTree, commonInput, sendFirstValue);
    }

    private ProtocolTree generateProtocolTree(SpendDeductTree spendDeductTree, CommonInput commonInput, SendFirstValue sendFirstValue) {
        if (spendDeductTree instanceof SpendDeductOrNode) {
            SpendDeductOrNode spendDeductOrNode = (SpendDeductOrNode) spendDeductTree;
            return or(
                    generateProtocolTree(spendDeductOrNode.getLeft(), commonInput, sendFirstValue),
                    generateProtocolTree(spendDeductOrNode.getRight(), commonInput, sendFirstValue)
            );
        } else if (spendDeductTree instanceof SpendDeductAndNode) {
            SpendDeductAndNode spendDeductAndNode = (SpendDeductAndNode) spendDeductTree;
            return and(
                    generateProtocolTree(spendDeductAndNode.getLeft(), commonInput, sendFirstValue),
                    generateProtocolTree(spendDeductAndNode.getRight(), commonInput, sendFirstValue)
            );
        } else if (spendDeductTree instanceof SpendDeductLeafNode) {
            SpendDeductLeafNode spendDeductLeafNode = (SpendDeductLeafNode) spendDeductTree;
            return leaf(
                    spendDeductLeafNode.getLeafName(),
                    spendDeductLeafNode.getProtocol(),
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
            putWitnesses(spendDeductOrNode.getLeft(), commonInput, secretInput, builder);
            putWitnesses(spendDeductOrNode.getRight(), commonInput, secretInput, builder);
        } else if (spendDeductTree instanceof SpendDeductAndNode) {
            SpendDeductAndNode spendDeductAndNode = (SpendDeductAndNode) spendDeductTree;
            putWitnesses(spendDeductAndNode.getLeft(), commonInput, secretInput, builder);
            putWitnesses(spendDeductAndNode.getRight(), commonInput, secretInput, builder);
        } else if (spendDeductTree instanceof SpendDeductLeafNode) {
            SpendDeductLeafNode spendDeductLeafNode = (SpendDeductLeafNode) spendDeductTree;
            if (spendDeductLeafNode.isTrue()) {
                builder.putSecretInput(spendDeductLeafNode.getLeafName(), spendDeductLeafNode.getWitness());
            }
        } else {
            throw new RuntimeException("Unexpected instance of SpendDeductTree found!");
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
