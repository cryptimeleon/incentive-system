package de.upb.crypto.incentive.cryptoprotocol.model.keys.provider;

import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import lombok.Data;

@Data
public class ProviderKeyPair implements Representable {
    @Represented
    private ProviderPublicKey pk;
    @Represented
    private ProviderSecretKey sk;

    public ProviderKeyPair(ProviderSecretKey sk, ProviderPublicKey pk) {
        this.sk = sk;
        this.pk = pk;
    }

    /**
     * constructor for construction of object by deserialization
     * @param repr serialized representation
     */
    public ProviderKeyPair(Representation repr)
    {
        new ReprUtil(this).deserialize(repr); // side effect reflection magic used to restore fields
    }

    public Representation getRepresentation()
    {
        return ReprUtil.serialize(this);
    }
}
