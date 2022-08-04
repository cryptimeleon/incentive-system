package org.cryptimeleon.incentive.crypto.benchmark;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class BenchmarkConfig {
    private static final BilinearGroupChoice DEFAULT_GROUP = BilinearGroupChoice.Herumi_MCL;
    private static final int DEFAULT_SECURITY_PARAMETER = 128;

    int iterations;
    IncentiveSystem incentiveSystem;
    IncentivePublicParameters pp;
    ProviderPublicKey ppk;
    ProviderSecretKey psk;
    UserPublicKey upk;
    UserSecretKey usk;

    /**
     * Create benchmark config with default configuration (see constant fields) with defined number of iterations.
     * Generates fresh incentive system parameters and keys.
     *
     * @param iterations number of iterations for each protocol.
     */
    public BenchmarkConfig(int iterations) {
        this.iterations = iterations;
        manualSetup(DEFAULT_SECURITY_PARAMETER, DEFAULT_GROUP);
    }

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
        var userKeys = Util.addGenesisSignatureToUserKeys(userPreKeys, providerKeys, pp);
        this.upk = userKeys.getPk();
        this.usk = userKeys.getSk();
        this.ppk = providerKeys.getPk();
        this.psk = providerKeys.getSk();
        this.incentiveSystem = new IncentiveSystem(pp);
    }
}
