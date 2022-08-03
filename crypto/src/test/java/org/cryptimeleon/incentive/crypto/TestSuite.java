package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;

public class TestSuite {
    static public final IncentivePublicParameters pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
    static public final IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
    static public final ProviderKeyPair providerKeyPair = incentiveSystem.generateProviderKeys();
    static public final UserKeyPair userKeyPair = incentiveSystem.generateUserKeys();
    static public final ProviderSecretKey providerSecretKey = providerKeyPair.getSk();
    static public final ProviderPublicKey providerPublicKey = providerKeyPair.getPk();
    static public final UserSecretKey userSecretKey = userKeyPair.getSk();
    static public final UserPublicKey userPublicKey = userKeyPair.getPk();
}
