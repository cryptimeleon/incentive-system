package org.cryptimeleon.incentive.services.dsprotection.storage;

import org.cryptimeleon.incentive.services.dsprotection.Util;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import javax.persistence.*;
import java.util.Objects;

/**
 * Data class for double-spending ID entries in the database ("DSID nodes").
 * Note that some attributes are serialized representations of the resembled properties since Hibernate
 * can only marshall objects that have primitive datatype fields only.
 */
@Entity
@Table(name = "dsids")
public class DsIdEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id; // identifier for entry in database table that represents this object
    @Column(name = "serializedDsidRepr", columnDefinition = "CLOB NOT NULL")
    @Lob
    private String serializedDsidRepr; // the actual double spending ID (from nice mathematical structure), in serialized-representation form
    private long associatedUserInfoId; // id of the user info entry for this dsid

    /**
     * Default (i.e. no args) constructor needed for ORM reasons
     */
    public DsIdEntry() {
    }

    /**
     * Standard constructor, does not associate user info to this token.
     */
    public DsIdEntry(Zn.ZnElement dsid) {
        this.serializedDsidRepr = Util.computeSerializedRepresentation(dsid);
    }

    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getSerializedDsidRepr() {
        return this.serializedDsidRepr;
    }

    public long getAssociatedUserInfoId() {
        return this.associatedUserInfoId;
    }

    public void setAssociatedUserInfoId(final long associatedUserInfoId) {
        this.associatedUserInfoId = associatedUserInfoId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof DsIdEntry)) return false;
        final DsIdEntry other = (DsIdEntry) o;
        if (!other.canEqual(this)) return false;
        if (this.getId() != other.getId()) return false;
        if (this.getAssociatedUserInfoId() != other.getAssociatedUserInfoId()) return false;
        final Object this$serializedDsidRepr = this.getSerializedDsidRepr();
        final Object other$serializedDsidRepr = other.getSerializedDsidRepr();
        return Objects.equals(this$serializedDsidRepr, other$serializedDsidRepr);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof DsIdEntry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $id = this.getId();
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        final long $associatedUserInfoId = this.getAssociatedUserInfoId();
        result = result * PRIME + (int) ($associatedUserInfoId >>> 32 ^ $associatedUserInfoId);
        final Object $serializedDsidRepr = this.getSerializedDsidRepr();
        result = result * PRIME + ($serializedDsidRepr == null ? 43 : $serializedDsidRepr.hashCode());
        return result;
    }
}
