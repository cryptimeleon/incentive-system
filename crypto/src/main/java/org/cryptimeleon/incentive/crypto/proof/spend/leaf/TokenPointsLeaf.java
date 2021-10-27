package org.cryptimeleon.incentive.crypto.proof.spend.leaf;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;

import java.math.BigInteger;

public class TokenPointsLeaf extends SpendDeductLeafNode {
    public final BigInteger[] lowerLimits;
    public final BigInteger[] upperLimits;


    public TokenPointsLeaf(String leafName, BigInteger[] lowerLimits, BigInteger[] upperLimits) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
    }

    public TokenPointsLeaf(String leafName, BigInteger[] lowerLimits, BigInteger[] upperLimits, boolean hasWitness) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
        this.setHasWitness(hasWitness);
    }
}
