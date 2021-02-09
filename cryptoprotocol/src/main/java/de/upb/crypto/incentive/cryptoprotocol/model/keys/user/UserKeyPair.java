package de.upb.crypto.incentive.cryptoprotocol.model.keys.user;

import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import lombok.Data;

@Data
public class UserKeyPair implements Representable {
    @Represented
    private UserPublicKey userPublicKey;
    @Represented
    private UserSecretKey userSecretKey;

    public UserKeyPair(UserPublicKey upk, UserSecretKey usk) {
        this.userPublicKey = upk;
        this.userSecretKey = usk;
    }

    /**
     * constructor for construction of object by deserialization
     * @param repr serialized representation
     */
    public UserKeyPair(Representation repr)
    {
        new ReprUtil(this).deserialize(repr); // side effect reflection magic used to restore fields
    }

    public Representation getRepresentation()
    {
        return ReprUtil.serialize(this);
    }
}
