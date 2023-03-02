package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.client.dto.ItemDto;
import org.cryptimeleon.incentive.client.dto.RewardItemDto;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.IncentiveSystemRestorer;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * Contains setup and helper methods for any test involving spend transactions.
 */
public class TransactionTestPreparation extends IncentiveSystemIntegrationTest {
    protected final String REWARD_ID = "TEST_REWARD_ID";

    protected final JSONConverter jsonConverter = new JSONConverter();

    protected final ZkpTokenUpdate testTokenUpdate = new HazelTokenUpdate(
            UUID.randomUUID(),
            "This is a test reward",
            new RewardSideEffect(REWARD_ID),
            20
    );
    protected final Promotion testPromotion = new HazelPromotion(
            HazelPromotion.generatePromotionParameters(),
            "Test Promotion",
            "Some Test Promotion",
            List.of(testTokenUpdate),
            "Apple");

    protected final ItemDto firstTestItem = new ItemDto("1", "First Test Item", 100);
    protected final ItemDto secondTestItem = new ItemDto("1", "First Test Item", 100);
    protected final List<ItemDto> testBasketItems = List.of(firstTestItem, secondTestItem);

    protected final ItemDto basketItemDto = new ItemDto("Some ID", "Apple", 1);
    protected final RewardItemDto rewardItemDto = new RewardItemDto(REWARD_ID, "Test Reward Item");

    protected InfoClient infoClient;
    protected BasketClient basketClient;
    protected IncentiveClient incentiveClient;

    protected TestCryptoAssets cryptoAssets;
    protected IncentiveSystem incentiveSystem;
    protected IncentiveSystemRestorer incentiveRestorer;


    protected void prepareBasketServiceAndPromotions() {
        infoClient = new InfoClient(infoUrl);
        basketClient = new BasketClient(basketUrl);
        incentiveClient = new IncentiveClient(incentiveUrl);

        cryptoAssets = TestHelper.getCryptoAssets(infoClient, providerSharedSecret, storeSharedSecret);
        incentiveSystem = new IncentiveSystem(cryptoAssets.getPublicParameters());
        incentiveRestorer = new IncentiveSystemRestorer(cryptoAssets.getPublicParameters());

        basketClient.newBasketItem(basketItemDto, basketProviderSecret).block();
        basketClient.newRewardItem(rewardItemDto, basketProviderSecret).block();
        basketClient.addShoppingItems(testBasketItems, basketProviderSecret);
        // Promotions need to be present at both services!
        basketClient.addPromotions(List.of(testPromotion), basketProviderSecret).block();
        incentiveClient.addPromotions(List.of(testPromotion), incentiveProviderSecret).block();
    }



    /*
     * helper methods
     */

    /**
     * wrapper around crypto. ... .Helper.generateToken that fixes parameters that are the same for all calls anyway.
     *
     * @param promotionParameters promotion parameters for the promotion the token should be used for
     * @param pointVector         point counts in the token
     * @return Token
     */
    protected Token generateToken(PromotionParameters promotionParameters, Vector<BigInteger> pointVector) {
        return Helper.generateToken(cryptoAssets.getPublicParameters(),
                cryptoAssets.getUserKeyPair(),
                cryptoAssets.getProviderKeyPair(),
                promotionParameters,
                pointVector
        );
    }

    /**
     * Generate empty token
     *
     * @return Token
     */
    protected Token generateToken() {
        return Helper.generateToken(cryptoAssets.getPublicParameters(),
                cryptoAssets.getUserKeyPair(),
                cryptoAssets.getProviderKeyPair(),
                testPromotion.getPromotionParameters()
        );
    }
}
