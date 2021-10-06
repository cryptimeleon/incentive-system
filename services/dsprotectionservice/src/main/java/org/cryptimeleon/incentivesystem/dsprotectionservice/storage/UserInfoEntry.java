package org.cryptimeleon.incentivesystem.dsprotectionservice.storage;

import lombok.Getter;
import org.apache.catalina.User;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.UserInfo;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.dsprotectionservice.Util;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import javax.persistence.*;


/**
 * Class storing some anonymous information about a user behind a token.
 * This includes the user's public key and the dsblame and dstrace information for that token.
 */

@Getter
@Entity
@Table(name="userInfo")
public class UserInfoEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String serializedUpkRepr;

    private String serializedDsBlameRepr;

    private String serializedDsTraceRepr;

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
        this.serializedDsTraceRepr = Util.computeSerializedRepresentation(uInfo.getDsBlame());
    }
}
