package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import lombok.Getter;
import org.cryptimeleon.math.structures.groups.GroupElement;

import javax.persistence.*;

/**
 * Data class double spending ID entries in the database.
 */
@Entity
@Getter
@Table(name="dsids")
public class DsIdEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id; // identifier for entry in database table that represents this object
    private GroupElement dsid; // the actual double spending ID (from nice mathematical structure)
    private long consumingTransactionId; // ID of the entry of the transaction consuming this token (realizes directed edge in the bipartite double-spending graph)

    public DsIdEntry(long id, GroupElement dsid) {
        this.id = id;
        this.dsid = dsid;
    }

    public DsIdEntry(long id, GroupElement dsid, long consumingTransactionId) {
        this.id = id;
        this.dsid = dsid;
        this.consumingTransactionId = consumingTransactionId;
    }

    public String toString(){
        return this.id + " " + this.dsid.toString();
    }
}
