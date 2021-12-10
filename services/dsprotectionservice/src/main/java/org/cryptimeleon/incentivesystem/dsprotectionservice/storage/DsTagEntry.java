package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import lombok.Getter;
import org.cryptimeleon.incentive.crypto.model.DoubleSpendingTag;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.dsprotectionservice.Util;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import javax.persistence.*;

/**
 * Data class representing a double-spending tag.
 * Needs ID attribute and some annotations to be processable by Hibernate (ORM framework).
 * The counterpart double-spending tag class in the crypto project does not have these but apart from that, the two classes are identical.
 *
 * Note that some attributes are serialized representations of the resembled properties since Hibernate
 * can only marshall objects that have primitive datatype fields only.
 */
@Entity
@Getter
@Table(name="dstags")
public class DsTagEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String serializedC0Repr; // challenge for deriving the user secret key

    private String serializedC1Repr; // challenge for deriving the encryption secret key

    private String serializedGammaRepr; // challenge generation helper value

    private String serializedEskStarProvRepr; // provider share for ElGamal encryption secret key

    @Column(name="serializedCTrace0Repr", columnDefinition="CLOB NOT NULL")
    @Lob
    private String serializedCTrace0Repr;

    @Column(name="serializedCTrace1Repr", columnDefinition="CLOB NOT NULL")
    @Lob
    private String serializedCTrace1Repr;

    /**
     * Default (i.e. no args) constructor needed for ORM reasons
     */
    public DsTagEntry() {}

    /**
     * Auto-generates the entry for a double-spending tag.
     */
    public DsTagEntry(DoubleSpendingTag dsTag) {
        this.serializedC0Repr = Util.computeSerializedRepresentation(dsTag.getC0());
        this.serializedC1Repr = Util.computeSerializedRepresentation(dsTag.getC1());
        this.serializedGammaRepr = Util.computeSerializedRepresentation(dsTag.getGamma());
        this.serializedEskStarProvRepr = Util.computeSerializedRepresentation(dsTag.getEskStarProv());
        this.serializedCTrace0Repr = Util.computeSerializedRepresentation(dsTag.getCtrace0());
        this.serializedCTrace1Repr = Util.computeSerializedRepresentation(dsTag.getCtrace1());
    }

    public DsTagEntry(String c0, String c1, String gamma, String eskStarProv, String ctrace0, String ctrace1) {
        this.serializedC0Repr = c0;
        this.serializedC1Repr = c1;
        this.serializedGammaRepr = gamma;
        this.serializedEskStarProvRepr = eskStarProv;
        this.serializedCTrace0Repr = ctrace0;
        this.serializedCTrace1Repr = ctrace1;
    }

    public DsTagEntry(String serializedDsTagRepr, IncentivePublicParameters pp) {
        // deserialize and restore ds tag
        JSONConverter jsonConverter = new JSONConverter();
        Representation dsTagRepr = jsonConverter.deserialize(serializedDsTagRepr);
        DoubleSpendingTag dsTag = new DoubleSpendingTag(dsTagRepr, pp);

        // assign fields
        this.serializedC0Repr = Util.computeSerializedRepresentation(dsTag.getC0());
        this.serializedC1Repr = Util.computeSerializedRepresentation(dsTag.getC1());
        this.serializedGammaRepr = Util.computeSerializedRepresentation(dsTag.getGamma());
        this.serializedEskStarProvRepr = Util.computeSerializedRepresentation(dsTag.getEskStarProv());
        this.serializedCTrace0Repr = Util.computeSerializedRepresentation(dsTag.getCtrace0());
        this.serializedCTrace1Repr = Util.computeSerializedRepresentation(dsTag.getCtrace1());
    }

    public String toString() {
        return this.id
                + " " + serializedC0Repr
                + " " + serializedC1Repr
                + " " + serializedGammaRepr
                + " " + serializedEskStarProvRepr
                + " " + serializedCTrace0Repr
                + " " + serializedCTrace1Repr;
    }

    @Override
    public boolean equals(Object o) {
        if(!o.getClass().equals(DsTagEntry.class)) {
            return false;
        }
        else {
            DsTagEntry otherDsTag = (DsTagEntry) o;
            return this.id == otherDsTag.getId()
                    && this.serializedC0Repr.equals(otherDsTag.getSerializedC0Repr())
                    && this.serializedC1Repr.equals(otherDsTag.getSerializedC1Repr())
                    && this.serializedGammaRepr.equals(otherDsTag.getSerializedGammaRepr())
                    && this.serializedEskStarProvRepr.equals(otherDsTag.getSerializedEskStarProvRepr())
                    && this.serializedCTrace0Repr.equals(otherDsTag.getSerializedCTrace0Repr())
                    && this.serializedCTrace1Repr.equals(otherDsTag.getSerializedCTrace1Repr());
        }
    }
}
