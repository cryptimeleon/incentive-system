package org.cryptimeleon.incentive.services.dsprotection.storage;

import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.services.dsprotection.Util;

import javax.persistence.*;
import java.util.Objects;

/**
 * Data class representing a double-spending tag.
 * Needs ID attribute and some annotations to be processable by Hibernate (ORM framework).
 * The counterpart double-spending tag class in the crypto project does not have these but apart from that, the two classes are identical.
 * <p>
 * Note that some attributes are serialized representations of the resembled properties since Hibernate
 * can only marshall objects that have primitive datatype fields only.
 */
@Entity
@Table(name = "dstags")
public class DsTagEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String serializedC0Repr; // challenge for deriving the user secret key
    private String serializedC1Repr; // challenge for deriving the encryption secret key
    private String serializedGammaRepr; // challenge generation helper value
    private String serializedEskStarProvRepr; // provider share for ElGamal encryption secret key
    @Column(name = "serializedCTrace0Repr", columnDefinition = "CLOB NOT NULL")
    @Lob
    private String serializedCTrace0Repr;
    @Column(name = "serializedCTrace1Repr", columnDefinition = "CLOB NOT NULL")
    @Lob
    private String serializedCTrace1Repr;

    /**
     * Default (i.e. no args) constructor needed for ORM reasons
     */
    public DsTagEntry() {
    }

    /**
     * Auto-generates the entry for a double-spending tag.
     */
    public DsTagEntry(DoubleSpendingTag dsTag) {
        // TODO decide what to do with this service
        this.serializedC0Repr = "";
        this.serializedC1Repr = "";
        this.serializedGammaRepr = Util.computeSerializedRepresentation(dsTag.getGamma());
        this.serializedEskStarProvRepr = "";
        this.serializedCTrace0Repr = "";
        this.serializedCTrace1Repr = "";
    }

    public String toString() {
        return this.id + " " + serializedC0Repr + " " + serializedC1Repr + " " + serializedGammaRepr + " " + serializedEskStarProvRepr + " " + serializedCTrace0Repr + " " + serializedCTrace1Repr;
    }

    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getSerializedC0Repr() {
        return this.serializedC0Repr;
    }

    public String getSerializedC1Repr() {
        return this.serializedC1Repr;
    }

    public String getSerializedGammaRepr() {
        return this.serializedGammaRepr;
    }

    public String getSerializedEskStarProvRepr() {
        return this.serializedEskStarProvRepr;
    }

    public String getSerializedCTrace0Repr() {
        return this.serializedCTrace0Repr;
    }

    public String getSerializedCTrace1Repr() {
        return this.serializedCTrace1Repr;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DsTagEntry)) return false;
        final DsTagEntry other = (DsTagEntry) o;
        if (!other.canEqual(this)) return false;
        if (this.getId() != other.getId()) return false;
        final Object this$serializedC0Repr = this.getSerializedC0Repr();
        final Object other$serializedC0Repr = other.getSerializedC0Repr();
        if (!Objects.equals(this$serializedC0Repr, other$serializedC0Repr))
            return false;
        final Object this$serializedC1Repr = this.getSerializedC1Repr();
        final Object other$serializedC1Repr = other.getSerializedC1Repr();
        if (!Objects.equals(this$serializedC1Repr, other$serializedC1Repr))
            return false;
        final Object this$serializedGammaRepr = this.getSerializedGammaRepr();
        final Object other$serializedGammaRepr = other.getSerializedGammaRepr();
        if (!Objects.equals(this$serializedGammaRepr, other$serializedGammaRepr))
            return false;
        final Object this$serializedEskStarProvRepr = this.getSerializedEskStarProvRepr();
        final Object other$serializedEskStarProvRepr = other.getSerializedEskStarProvRepr();
        if (!Objects.equals(this$serializedEskStarProvRepr, other$serializedEskStarProvRepr))
            return false;
        final Object this$serializedCTrace0Repr = this.getSerializedCTrace0Repr();
        final Object other$serializedCTrace0Repr = other.getSerializedCTrace0Repr();
        if (!Objects.equals(this$serializedCTrace0Repr, other$serializedCTrace0Repr))
            return false;
        final Object this$serializedCTrace1Repr = this.getSerializedCTrace1Repr();
        final Object other$serializedCTrace1Repr = other.getSerializedCTrace1Repr();
        return Objects.equals(this$serializedCTrace1Repr, other$serializedCTrace1Repr);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DsTagEntry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $id = this.getId();
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        final Object $serializedC0Repr = this.getSerializedC0Repr();
        result = result * PRIME + ($serializedC0Repr == null ? 43 : $serializedC0Repr.hashCode());
        final Object $serializedC1Repr = this.getSerializedC1Repr();
        result = result * PRIME + ($serializedC1Repr == null ? 43 : $serializedC1Repr.hashCode());
        final Object $serializedGammaRepr = this.getSerializedGammaRepr();
        result = result * PRIME + ($serializedGammaRepr == null ? 43 : $serializedGammaRepr.hashCode());
        final Object $serializedEskStarProvRepr = this.getSerializedEskStarProvRepr();
        result = result * PRIME + ($serializedEskStarProvRepr == null ? 43 : $serializedEskStarProvRepr.hashCode());
        final Object $serializedCTrace0Repr = this.getSerializedCTrace0Repr();
        result = result * PRIME + ($serializedCTrace0Repr == null ? 43 : $serializedCTrace0Repr.hashCode());
        final Object $serializedCTrace1Repr = this.getSerializedCTrace1Repr();
        result = result * PRIME + ($serializedCTrace1Repr == null ? 43 : $serializedCTrace1Repr.hashCode());
        return result;
    }
}
