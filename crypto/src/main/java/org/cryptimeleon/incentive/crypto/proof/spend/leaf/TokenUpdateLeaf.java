package org.cryptimeleon.incentive.crypto.proof.spend.leaf;

import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductLeafNode;

import java.math.BigInteger;

public class TokenUpdateLeaf extends SpendDeductLeafNode {

    public final BigInteger[] lowerLimits;
    public final BigInteger[] upperLimits;
    public final BigInteger[] aVector;
    public final BigInteger[] bVector;

    public TokenUpdateLeaf(String leafName, BigInteger[] lowerLimits, BigInteger[] upperLimits, BigInteger[] aVector, BigInteger[] bVector) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
        this.aVector = aVector;
        this.bVector = bVector;
    }

    public TokenUpdateLeaf(String leafName, BigInteger[] lowerLimits, BigInteger[] upperLimits, BigInteger[] aVector, BigInteger[] bVector, boolean hasWitness) {
        super(leafName);
        this.lowerLimits = lowerLimits;
        this.upperLimits = upperLimits;
        this.aVector = aVector;
        this.bVector = bVector;
        this.setHasWitness(hasWitness);
    }
}
