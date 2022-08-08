package org.cryptimeleon.incentive.crypto.model.keys.user;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;

@Value
@AllArgsConstructor
public class UserKeyPair {
    UserPublicKey pk;
    UserSecretKey sk;

    public UserKeyPair(UserPreKeyPair userPreKeyPair, SPSEQSignature genesisSignature) {
        pk = userPreKeyPair.getPk();
        sk = new UserSecretKey(userPreKeyPair.getPsk(), genesisSignature);
    }

}
