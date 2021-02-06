package de.upb.crypto.incentive.services.credit;

import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.CreditInterface;
import de.upb.crypto.incentive.services.credit.mock.TestBasketServerClientMock;
import de.upb.crypto.incentive.services.credit.mock.TestCryptoCreditMock;
import de.upb.crypto.incentive.services.credit.model.interfaces.BasketServerClientInterface;
import org.springframework.context.annotation.Bean;

public class TestConfiguration {

    @Bean
    BasketServerClientInterface basketServerClientInterface() {
        return new TestBasketServerClientMock();
    }

    @Bean
    CreditInterface creditInterface() {
        return new TestCryptoCreditMock();
    }
}
