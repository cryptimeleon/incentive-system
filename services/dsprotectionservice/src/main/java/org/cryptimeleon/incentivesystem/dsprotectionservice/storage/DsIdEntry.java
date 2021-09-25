package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import org.cryptimeleon.math.structures.groups.GroupElement;

import javax.persistence.*;

/**
 * Data class double spending ID entries in the database.
 */
@Entity
@Table(name="dsids")
public class DsIdEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id; // identifier for entry in database table that represents this object
    private GroupElement dsid; // the actual double spending ID (from nice mathematical structure)

    public DsIdEntry() {}

    public DsIdEntry(long id, GroupElement dsid) {
        this.id = id;
        this.dsid = dsid;
    }

    public String toString(){
        return this.id + " " + this.dsid.toString();
    }
}
