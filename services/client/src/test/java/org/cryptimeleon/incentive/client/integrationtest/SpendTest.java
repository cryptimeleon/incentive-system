package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;


@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpendTest extends TransactionTestPreparation {


    @BeforeAll
    protected void prepareBasketServiceAndPromotions() {
        super.prepareBasketServiceAndPromotions();
    }

}
