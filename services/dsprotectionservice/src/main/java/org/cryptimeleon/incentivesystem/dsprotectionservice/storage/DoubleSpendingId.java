package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import org.cryptimeleon.math.structures.groups.GroupElement;

import javax.persistence.*;

/**
 * Data class for the double spending ID group element. Needed since every object processed by Hibernate ORM needs an integer ID.
 */
@Entity
@Table(name="dsids")
public class DoubleSpendingId {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id; // identifier for entry in database table that represents this object
    private GroupElement dsid; // the actual double spending ID (from nice mathematical structure)

    public DoubleSpendingId() {}

    public DoubleSpendingId(long id, GroupElement dsid) {
        this.id = id;
        this.dsid = dsid;
    }

    public String toString(){
        return this.id + " " + this.dsid.toString();
    }
}
