package org.cryptimeleon.incentive.crypto.model;

import java.util.Objects;

/**
 * Tuple for the private and public output of the deduct algorithm executed by the provider when processing an spend request.
 */
public class DeductOutput {
    private final SpendResponse spendResponse;
    private final DoubleSpendingTag dstag;

    public DeductOutput(SpendResponse spendResponse, DoubleSpendingTag dstag) {
        this.spendResponse = spendResponse;
        this.dstag = dstag;
    }

    public SpendResponse getSpendResponse() {
        return this.spendResponse;
    }

    public DoubleSpendingTag getDstag() {
        return this.dstag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeductOutput that = (DeductOutput) o;
        return Objects.equals(spendResponse, that.spendResponse) && Objects.equals(dstag, that.dstag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spendResponse, dstag);
    }

    public String toString() {
        return "DeductOutput(spendResponse=" + this.getSpendResponse() + ", dstag=" + this.getDstag() + ")";
    }
}
