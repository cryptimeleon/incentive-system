package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import lombok.Getter;
import lombok.Setter;
import org.cryptimeleon.math.structures.groups.GroupElement;

import javax.persistence.*;

/**
 * Data class for double-spending ID entries in the database ("DSID nodes").
 * Note that some attributes are serialized representations of the resembled properties since Hibernate
 * can only marshall objects that have primitive datatype fields only.
 */
@Entity
@Getter
@Setter
@Table(name="dsids")
public class DsIdEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id; // identifier for entry in database table that represents this object
    private String serializedDsidRepr; // the actual double spending ID (from nice mathematical structure), in serialized-representation form
    private long associatedUserInfoId; // id of the user info entry for this dsid


    public DsIdEntry(String serializedDsidRepr) {
        this.serializedDsidRepr = serializedDsidRepr;
    }

    public DsIdEntry(long id, String serializedDsidRepr, long associatedUserInfoId) {
        this.id = id;
        this.serializedDsidRepr = serializedDsidRepr;
        this.associatedUserInfoId = associatedUserInfoId;
    }
}