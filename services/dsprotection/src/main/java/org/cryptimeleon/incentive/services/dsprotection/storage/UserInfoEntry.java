package org.cryptimeleon.incentive.services.dsprotection.storage;

import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentive.services.dsprotection.Util;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class storing some anonymous information about a user behind a token.
 * This includes the user's public key and the dsblame and dstrace information for that token.
 */
@Entity
@Table(name = "userInfo")
public class UserInfoEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "serializedUpkRepr", columnDefinition = "CLOB NOT NULL")
    @Lob
    private String serializedUpkRepr;
    private String serializedDsBlameRepr;
    private String serializedDsTraceRepr;

    /**
     * Default (i.e. no args) constructor needed for ORM reasons
     */
    public UserInfoEntry() {
    }

    /**
     * Converts a user info (crypto) object to a user info entry.
     */
    public UserInfoEntry(UserInfo userInfo) {
        this.serializedUpkRepr = Util.computeSerializedRepresentation(userInfo.getUpk());
        this.serializedDsBlameRepr = Util.computeSerializedRepresentation(userInfo.getDsBlame());
        this.serializedDsTraceRepr = Util.computeSerializedRepresentation(userInfo.getDsTrace());
    }

    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getSerializedUpkRepr() {
        return this.serializedUpkRepr;
    }

    public String getSerializedDsBlameRepr() {
        return this.serializedDsBlameRepr;
    }

    public String getSerializedDsTraceRepr() {
        return this.serializedDsTraceRepr;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UserInfoEntry)) return false;
        final UserInfoEntry other = (UserInfoEntry) o;
        if (!other.canEqual(this)) return false;
        if (this.getId() != other.getId()) return false;
        final Object this$serializedUpkRepr = this.getSerializedUpkRepr();
        final Object other$serializedUpkRepr = other.getSerializedUpkRepr();
        if (!Objects.equals(this$serializedUpkRepr, other$serializedUpkRepr))
            return false;
        final Object this$serializedDsBlameRepr = this.getSerializedDsBlameRepr();
        final Object other$serializedDsBlameRepr = other.getSerializedDsBlameRepr();
        if (!Objects.equals(this$serializedDsBlameRepr, other$serializedDsBlameRepr))
            return false;
        final Object this$serializedDsTraceRepr = this.getSerializedDsTraceRepr();
        final Object other$serializedDsTraceRepr = other.getSerializedDsTraceRepr();
        return Objects.equals(this$serializedDsTraceRepr, other$serializedDsTraceRepr);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UserInfoEntry;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $id = this.getId();
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        final Object $serializedUpkRepr = this.getSerializedUpkRepr();
        result = result * PRIME + ($serializedUpkRepr == null ? 43 : $serializedUpkRepr.hashCode());
        final Object $serializedDsBlameRepr = this.getSerializedDsBlameRepr();
        result = result * PRIME + ($serializedDsBlameRepr == null ? 43 : $serializedDsBlameRepr.hashCode());
        final Object $serializedDsTraceRepr = this.getSerializedDsTraceRepr();
        result = result * PRIME + ($serializedDsTraceRepr == null ? 43 : $serializedDsTraceRepr.hashCode());
        return result;
    }
}
