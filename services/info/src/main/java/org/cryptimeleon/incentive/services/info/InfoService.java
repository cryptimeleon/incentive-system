package org.cryptimeleon.incentive.services.info;

import org.cryptimeleon.incentive.crypto.BilinearGroupChoice;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InfoService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InfoService.class);
    private String serializedPublicParameters;
    private String serializedProviderPublicKey;
    private String serializedProviderSecretKey;
    private String serializedStorePublicKey;
    private String serializedStoreSecretKey;
    @Value("${provider.shared-secret}")
    private String providerSharedSecret;

    @Value("${store.shared-secret}")

    private String storeSharedSecret;

    @Value("${info.use-mcl}")
    private boolean useMcl;

    public InfoService() {
    }

    public boolean verifyProviderSharedSecret(String providerSharedSecret) {
        return this.providerSharedSecret.equals(providerSharedSecret);
    }

    public boolean verifyStoreSharedSecret(String storeSharedSecret) {
        return this.storeSharedSecret.equals(storeSharedSecret);
    }

    @PostConstruct
    public void init() {
        // Make sure shared secret is set
        if (providerSharedSecret.equals("")) {
            throw new IllegalArgumentException("Provider's shared secret is not set.");
        }
        if (storeSharedSecret.equals("")) {
            throw new IllegalArgumentException("Store's shared secret is not set.");
        }
        log.info("Shared secret: {}", providerSharedSecret);
        log.info("Shared secret: {}", storeSharedSecret);

        log.info("Setting up a new incentive-system");
        IncentivePublicParameters pp;
        if (useMcl) {
            log.info("Generate pp using mcl");
            pp = Setup.trustedSetup(128, BilinearGroupChoice.Herumi_MCL);
        } else {
            log.info("Generate pp using debug group");
            pp = Setup.trustedSetup(128, BilinearGroupChoice.Debug);
        }

        log.info("Generate provider keypair");
        ProviderKeyPair providerKeyPair = Setup.providerKeyGen(pp);

        log.info("Generate store keypair");
        StoreKeyPair storeKeyPair = Setup.storeKeyGen();

        log.info("Serializing pp and keypair");
        JSONConverter jsonConverter = new JSONConverter();
        serializedPublicParameters = jsonConverter.serialize(pp.getRepresentation());
        serializedProviderPublicKey = jsonConverter.serialize(providerKeyPair.getPk().getRepresentation());
        serializedProviderSecretKey = jsonConverter.serialize(providerKeyPair.getSk().getRepresentation());
        serializedStorePublicKey = jsonConverter.serialize(storeKeyPair.getPk().getRepresentation());
        serializedStoreSecretKey = jsonConverter.serialize(storeKeyPair.getSk().getRepresentation());
        log.info("Setup finished");
    }

    public String getSerializedPublicParameters() {
        return this.serializedPublicParameters;
    }

    public String getSerializedProviderPublicKey() {
        return this.serializedProviderPublicKey;
    }

    public String getSerializedProviderSecretKey() {
        return this.serializedProviderSecretKey;
    }

    public String getSerializedStorePublicKey() {
        return this.serializedStorePublicKey;
    }

    public String getSerializedStoreSecretKey() {
        return this.serializedStoreSecretKey;
    }
}
