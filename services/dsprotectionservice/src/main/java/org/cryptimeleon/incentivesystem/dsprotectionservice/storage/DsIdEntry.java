package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import lombok.Getter;
import lombok.Setter;
import org.cryptimeleon.math.structures.groups.GroupElement;

import javax.persistence.*;

/**
 * Data class double spending ID entries in the database.
 */
@Entity
@Getter
@Setter
@Table(name="dsids")
public class DsIdEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id; // identifier for entry in database table that represents this object
    private GroupElement dsid; // the actual double spending ID (from nice mathematical structure)
    private long associatedUserInfoId; // id of the user info entry for this dsid
    private long consumingTransactionId; // ID of the entry of the transaction consuming this token (realizes directed edge in the bipartite double-spending graph)


    public DsIdEntry(GroupElement dsid) {
        this.dsid = dsid;
    }

    public DsIdEntry(long id, GroupElement dsid, long associatedUserInfoId, long consumingTransactionId) {
        this.id = id;
        this.dsid = dsid;
        this.associatedUserInfoId = associatedUserInfoId;
        this.consumingTransactionId = consumingTransactionId;
    }
}
