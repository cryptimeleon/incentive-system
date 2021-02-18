package de.upb.crypto.incentive.services.credit.config;

import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.CreditInterface;
import de.upb.crypto.incentive.services.credit.BasketServerClientHelper;
import de.upb.crypto.incentive.services.credit.mock.CryptoCreditMock;
import de.upb.crypto.incentive.services.credit.model.interfaces.BasketServerClientInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * Set up Beans such that mocks are used until actual implementation is provided
 */
@Configuration
public class CreditConfiguration {

    @Value("${basketserver.url}")
    private String basketServerUrl;

    @Value("${basketserver.redeem-secret}")
    private String redeemSecret;

    @Bean
    @ConditionalOnMissingBean
    BasketServerClientInterface basketServerClientInterface() {
        return new BasketServerClientHelper(basketServerUrl, redeemSecret);
    }

    @Bean
    @ConditionalOnMissingBean
    CreditInterface creditInterface() {
        return new CryptoCreditMock();
    }
}