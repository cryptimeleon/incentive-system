package org.cryptimeleon.incentive.crypto.benchmark;

import org.cryptimeleon.incentive.crypto.BilinearGroupChoice;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;

/**
 * A simple data class for a benchmark configuration with different constructors.
 */
public class BenchmarkConfig {
    int iterations;
    IncentiveSystem incentiveSystem;
    IncentivePublicParameters pp;
    ProviderPublicKey ppk;
    ProviderSecretKey psk;
    UserPublicKey upk;
    UserSecretKey usk;

    /**
     * Create benchmark config with with defined number of iterations, security parameters and group.
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

    public BenchmarkConfig(int iterations, IncentiveSystem incentiveSystem, IncentivePublicParameters pp, ProviderPublicKey ppk, ProviderSecretKey psk, UserPublicKey upk, UserSecretKey usk) {
        this.iterations = iterations;
        this.incentiveSystem = incentiveSystem;
        this.pp = pp;
        this.ppk = ppk;
        this.psk = psk;
        this.upk = upk;
        this.usk = usk;
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
        this.upk = userKeys.getPk();
        this.usk = userKeys.getSk();
        this.ppk = providerKeys.getPk();
        this.psk = providerKeys.getSk();
        this.incentiveSystem = new IncentiveSystem(pp);
    }
}
