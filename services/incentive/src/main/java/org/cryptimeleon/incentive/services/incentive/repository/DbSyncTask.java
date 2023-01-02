package org.cryptimeleon.incentive.services.incentive.repository;

import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Data class modelling a single transaction that needs to be synced into the double-spending protection database.
 */
public final class DbSyncTask {
    private final BigInteger promotionId; // ID of the promotion that the transaction exploits
    private final Zn.ZnElement tid; // ID of the transaction
    private final SpendRequest spendRequest; // object describing the spend request that lead to the transaction
    private final DeductOutput deductOutput; // output (= spend response + double-spending tag) that the provider generated when processing the above spend request

    public DbSyncTask(final BigInteger promotionId, final Zn.ZnElement tid, final SpendRequest spendRequest, final DeductOutput deductOutput) {
        this.promotionId = promotionId;
        this.tid = tid;
        this.spendRequest = spendRequest;
        this.deductOutput = deductOutput;
    }

    public BigInteger getPromotionId() {
        return this.promotionId;
    }

    public Zn.ZnElement getTid() {
        return this.tid;
    }

    public SpendRequest getSpendRequest() {
        return this.spendRequest;
    }

    public DeductOutput getDeductOutput() {
        return this.deductOutput;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DbSyncTask)) return false;
        final DbSyncTask other = (DbSyncTask) o;
        final Object this$promotionId = this.getPromotionId();
        final Object other$promotionId = other.getPromotionId();
        if (!Objects.equals(this$promotionId, other$promotionId))
            return false;
        final Object this$tid = this.getTid();
        final Object other$tid = other.getTid();
        if (!Objects.equals(this$tid, other$tid)) return false;
        final Object this$spendRequest = this.getSpendRequest();
        final Object other$spendRequest = other.getSpendRequest();
        if (!Objects.equals(this$spendRequest, other$spendRequest))
            return false;
        final Object this$deductOutput = this.getDeductOutput();
        final Object other$deductOutput = other.getDeductOutput();
        return Objects.equals(this$deductOutput, other$deductOutput);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $promotionId = this.getPromotionId();
        result = result * PRIME + ($promotionId == null ? 43 : $promotionId.hashCode());
        final Object $tid = this.getTid();
        result = result * PRIME + ($tid == null ? 43 : $tid.hashCode());
        final Object $spendRequest = this.getSpendRequest();
        result = result * PRIME + ($spendRequest == null ? 43 : $spendRequest.hashCode());
        final Object $deductOutput = this.getDeductOutput();
        result = result * PRIME + ($deductOutput == null ? 43 : $deductOutput.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "DbSyncTask(promotionId=" + this.getPromotionId() + ", tid=" + this.getTid() + ", spendRequest=" + this.getSpendRequest() + ", deductOutput=" + this.getDeductOutput() + ")";
    }
}
