package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpendTest extends TransactionTestPreparation {


    @BeforeAll
    protected void prepareBasketAndPromotions() {
        super.prepareBasketAndPromotions();
    }

    @Test
    void rewardsAddedToBasketTest() {
        Token token = generateToken(
                testPromotion.getPromotionParameters(),
                Vector.of(BigInteger.valueOf(20))
        );
        var basketId = createBasket();
        assert basketId != null;
        log.info("BasketId: " + basketId.toString());

        runSpendDeductWorkflow(token, basketId);
        var basketAfterSpend = basketClient.getBasket(basketId).block();

        assert basketAfterSpend != null;
        assertThat(basketAfterSpend.getRewardItems()).containsExactly(REWARD_ID);
    }
}
