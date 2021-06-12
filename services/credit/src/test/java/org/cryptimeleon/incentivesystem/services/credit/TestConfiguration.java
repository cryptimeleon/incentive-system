package org.cryptimeleon.incentivesystem.services.credit;

import org.cryptimeleon.incentivesystem.services.credit.interfaces.CreditInterface;
import org.cryptimeleon.incentivesystem.services.credit.mock.TestBasketServerClientMock;
import org.cryptimeleon.incentivesystem.services.credit.mock.TestCryptoCreditMock;
import org.cryptimeleon.incentivesystem.services.credit.model.interfaces.BasketServerClientInterface;
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
