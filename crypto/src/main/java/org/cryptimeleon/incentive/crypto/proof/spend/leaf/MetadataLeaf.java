package org.cryptimeleon.incentive.crypto.proof.spend.leaf;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;

/**
 * Leaf node for a ZKP that checks the metadata of the tokens for which the user must always know a witness.
 * Since it does not make sense to have two of these in the same {@link SpendDeductBooleanZkp},
 * the name constant.
 */
public class MetadataLeaf extends SpendDeductLeafNode {

    public MetadataLeaf() {
        super("MetadataZkp");
        setHasWitness(true);
    }
}
