package org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark;

import lombok.AllArgsConstructor;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.Setup;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey;

@AllArgsConstructor
public class BenchmarkConfig {
    private static final Setup.BilinearGroupChoice DEFAULT_GROUP = Setup.BilinearGroupChoice.Herumi_MCL;
    private static final int DEFAULT_SECURITY_PARAMETER = 128;

    int iterations;
    IncentiveSystem incentiveSystem;
    IncentivePublicParameters pp;
    ProviderPublicKey ppk;
    ProviderSecretKey psk;
    UserPublicKey upk;
    UserSecretKey usk;

    public BenchmarkConfig(int iterations) {
        this.iterations = iterations;
        manualSetup(DEFAULT_SECURITY_PARAMETER, DEFAULT_GROUP);
    }

    public BenchmarkConfig(int iterations, int securityParameter, Setup.BilinearGroupChoice bilinearGroupChoice) {
        this.iterations = iterations;
        manualSetup(securityParameter, bilinearGroupChoice);
    }

    private void manualSetup(int securityParameter, Setup.BilinearGroupChoice bilinearGroupChoice) {
        this.pp = Setup.trustedSetup(securityParameter, bilinearGroupChoice);
        var providerKeys = Setup.providerKeyGen(pp);
        var userKeys = Setup.userKeyGen(pp);
        this.upk = userKeys.getPk();
        this.usk = userKeys.getSk();
        this.ppk = providerKeys.getPk();
        this.psk = providerKeys.getSk();
        this.incentiveSystem = new IncentiveSystem(pp);
    }

    public int getIterations() {
        return iterations;
    }
}
