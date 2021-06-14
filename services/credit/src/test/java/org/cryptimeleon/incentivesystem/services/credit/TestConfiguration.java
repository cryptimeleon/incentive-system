package org.cryptimeleon.incentivesystem.services.credit;

import org.cryptimeleon.incentivesystem.services.credit.interfaces.CreditInterface;
import org.cryptimeleon.incentivesystem.services.credit.mock.TestBasketClientMock;
import org.cryptimeleon.incentivesystem.services.credit.mock.TestCryptoCreditMock;
import org.cryptimeleon.incentivesystem.services.credit.model.interfaces.BasketClientInterface;
import org.springframework.context.annotation.Bean;

public class TestConfiguration {

    @Bean
    BasketClientInterface basketClientInterface() {
        return new TestBasketClientMock();
    }

    @Bean
    CreditInterface creditInterface() {
        return new TestCryptoCreditMock();
    }
}
