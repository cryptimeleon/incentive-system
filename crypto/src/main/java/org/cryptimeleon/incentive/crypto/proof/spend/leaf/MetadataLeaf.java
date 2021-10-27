package org.cryptimeleon.incentive.crypto.proof.spend.leaf;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;

public class MetadataLeaf extends SpendDeductLeafNode {

    public MetadataLeaf() {
        super("MetadataZkp");
        setHasWitness(true);
    }
}
