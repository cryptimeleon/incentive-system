package org.cryptimeleon.incentive.services.credit;

import org.cryptimeleon.incentive.services.credit.interfaces.CreditInterface;
import org.cryptimeleon.incentive.services.credit.mock.TestBasketClientMock;
import org.cryptimeleon.incentive.services.credit.mock.TestCryptoCreditMock;
import org.cryptimeleon.incentive.services.credit.interfaces.BasketClientInterface;
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
