package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import lombok.Getter;
import lombok.Setter;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentivesystem.dsprotectionservice.Util;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import javax.persistence.*;


/**
 * Class storing some anonymous information about a user behind a token.
 * This includes the user's public key and the dsblame and dstrace information for that token.
 */

@Getter
@Setter
@Entity
@Table(name="userInfo")
public class UserInfoEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name="serializedUpkRepr", columnDefinition="CLOB NOT NULL")
    @Lob
    private String serializedUpkRepr;

    private String serializedDsBlameRepr;

    private String serializedDsTraceRepr;

    /**
     * Default (i.e. no args) constructor needed for ORM reasons
     */
    public UserInfoEntry() {}

    /**
     * All args constructor, note that ID is auto-generated.
     */
    public UserInfoEntry(String upk, String dsBlame, String dsTrace) {
        this.serializedUpkRepr = upk;
        this.serializedDsBlameRepr = dsBlame;
        this.serializedDsTraceRepr = dsTrace;
    }

    /**
     * Converts a user info (crypto) object to a user info entry.
     */
    public UserInfoEntry(UserInfo userInfo){
        this.serializedUpkRepr = Util.computeSerializedRepresentation(userInfo.getUpk());
        this.serializedDsBlameRepr = Util.computeSerializedRepresentation(userInfo.getDsBlame());
        this.serializedDsTraceRepr = Util.computeSerializedRepresentation(userInfo.getDsTrace());
    }

    /**
     * Constructs a user info entry from a serialized user info representation (crypto object).
     * @param serializedUserInfoRepr serialized representation a user info object
     * @param pp public parameters of the respective incentive system instance
     */
    public UserInfoEntry(String serializedUserInfoRepr, IncentivePublicParameters pp) {
        // deserialize user info
        JSONConverter jsonConverter = new JSONConverter();
        Representation uInfoRepr = jsonConverter.deserialize(serializedUserInfoRepr);
        UserInfo uInfo = new UserInfo(uInfoRepr, pp);

        // initialize entry object
        this.serializedUpkRepr = Util.computeSerializedRepresentation(uInfo.getUpk());
        this.serializedDsBlameRepr = Util.computeSerializedRepresentation(uInfo.getDsBlame());
        this.serializedDsTraceRepr = Util.computeSerializedRepresentation(uInfo.getDsTrace());
    }

    @Override
    public boolean equals(Object o) {
        if(!o.getClass().equals(UserInfoEntry.class)) {
            return false;
        }
        else {
            UserInfoEntry otherUInfoEntry = (UserInfoEntry) o;
            return this.id == otherUInfoEntry.getId()
                    && this.serializedUpkRepr.equals(otherUInfoEntry.getSerializedUpkRepr())
                    && this.serializedDsTraceRepr.equals(otherUInfoEntry.getSerializedDsTraceRepr())
                    && this.serializedDsBlameRepr.equals(otherUInfoEntry.getSerializedDsBlameRepr());
        }
    }
}
