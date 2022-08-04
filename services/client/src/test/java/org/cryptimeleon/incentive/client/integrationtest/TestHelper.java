package org.cryptimeleon.incentive.client.integrationtest;

import lombok.Value;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.client.dto.BasketDto;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

import java.util.UUID;


/**
 * Some utility workflows that can be re-used.
 */
public class TestHelper {

    private static final JSONConverter jsonConverter = new JSONConverter();

    /**
     * Create a basket and return basket object.
     *
     * @param basketUrl url of basket service
     * @return basket object that was added to basket service
     */
    static BasketDto createBasketWithItems(String basketUrl) {
        var basketClient = new BasketClient(basketUrl);

        UUID basketId = basketClient.createBasket().block();
        var items = basketClient.getItems().block();
        var firstTestItem = items[0];
        var secondTestItem = items[1];

        basketClient.putItemToBasket(basketId, firstTestItem.getId(), 3).block();
        basketClient.putItemToBasket(basketId, secondTestItem.getId(), 1).block();

        return basketClient.getBasket(basketId).block();
    }

    static TestCryptoAssets getCryptoAssets(InfoClient infoClient, String providerSharedSecret) {
        IncentivePublicParameters pp = new IncentivePublicParameters(jsonConverter.deserialize(infoClient.querySerializedPublicParameters().block()));
        ProviderSecretKey providerSecretKey = new ProviderSecretKey(jsonConverter.deserialize(infoClient.querySerializedProviderSecretKey(providerSharedSecret).block()), pp);
        ProviderPublicKey providerPublicKey = new ProviderPublicKey(jsonConverter.deserialize(infoClient.querySerializedProviderPublicKey().block()), pp);
        ProviderKeyPair providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        UserPreKeyPair userPreKeyPair = (new IncentiveSystem(pp)).generateUserPreKeyPair();
        UserKeyPair userKeyPair = Util.addGenesisSignatureToUserKeys(userPreKeyPair, providerKeyPair, pp);

        return new TestCryptoAssets(pp, providerKeyPair, userKeyPair);
    }
}

@Value
class TestCryptoAssets {
    IncentivePublicParameters publicParameters;
    ProviderKeyPair providerKeyPair;
    UserKeyPair userKeyPair;
}