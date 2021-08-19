package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import javax.persistence.*;

/**
 * Data class representing a double-spending tag.
 * Needs ID attribute and some annotations to be processable by Hibernate (ORM framework).
 * The counterpart double-spending tag class in the crypto project does not have these but apart from that, the two classes are identical.
 */
@Entity
@Table(name="dstags")
public class DoubleSpendingTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Zn.ZnElement c0; // challenge for deriving the user secret key
    private Zn.ZnElement c1; // challenge for deriving the encryption secret key
    private Zn.ZnElement gamma; // challenge generation helper value
    private Zn.ZnElement eskStarProv; // provider share for ElGamal encryption secret key
    private GroupElementVector ctrace0;
    private GroupElementVector ctrace1;

    public DoubleSpendingTag() {

    }

    public DoubleSpendingTag(long id, Zn.ZnElement c0, Zn.ZnElement c1, Zn.ZnElement gamma, Zn.ZnElement eskStarProv, GroupElementVector ctrace0, GroupElementVector ctrace1) {
        this.id = id;
        this.c0 = c0;
        this.c1 = c1;
        this.gamma = gamma;
        this.eskStarProv = eskStarProv;
        this.ctrace0 = ctrace0;
        this.ctrace1 = ctrace1;
    }

    public String toString() {
        return this.id
                + " " + this.c0.toString()
                + " " + this.c1.toString()
                + " " + this.gamma.toString()
                + " " + this.eskStarProv.toString()
                + " " + this.ctrace0.toString()
                + " " + this.ctrace1.toString();
    }
}
