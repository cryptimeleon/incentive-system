package org.cryptimeleon.incentive.crypto.benchmark;

import org.cryptimeleon.incentive.crypto.BilinearGroupChoice;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;

/**
 * A simple data class for a benchmark configuration with different constructors.
 */
public class BenchmarkConfig {
    final int iterations;
    IncentiveSystem incentiveSystem;
    IncentivePublicParameters pp;
    ProviderPublicKey ppk;
    ProviderSecretKey psk;
    UserPublicKey upk;
    UserSecretKey usk;
    StorePublicKey spk;
    StoreSecretKey ssk;

    /**
     * Create benchmark config with defined number of iterations, security parameters and group.
     * Generates fresh incentive system parameters and keys.
     *
     * @param iterations          number of iterations for each protocol.
     * @param securityParameter   security parameter to use
     * @param bilinearGroupChoice the bilinear group that should be used
     */
    public BenchmarkConfig(int iterations, int securityParameter, BilinearGroupChoice bilinearGroupChoice) {
        this.iterations = iterations;
        this.manualSetup(securityParameter, bilinearGroupChoice);
    }

    /**
     * Utility function to generate incentive system parameters and keys according to other parameters.
     *
     * @param securityParameter   security parameter to use
     * @param bilinearGroupChoice bilinear group to use
     */
    private void manualSetup(int securityParameter, BilinearGroupChoice bilinearGroupChoice) {
        this.pp = Setup.trustedSetup(securityParameter, bilinearGroupChoice);
        var providerKeys = Setup.providerKeyGen(pp);
        var userPreKeys = Setup.userPreKeyGen(pp);
        var userKeys = Util.addRegistrationSignatureToUserPreKeys(userPreKeys, providerKeys, pp);
        var storeKeys = Setup.storeKeyGen();
        this.upk = userKeys.getPk();
        this.usk = userKeys.getSk();
        this.ppk = providerKeys.getPk();
        this.psk = providerKeys.getSk();
        this.spk = storeKeys.getPk();
        this.ssk = storeKeys.getSk();
        this.incentiveSystem = new IncentiveSystem(pp);
    }
}
