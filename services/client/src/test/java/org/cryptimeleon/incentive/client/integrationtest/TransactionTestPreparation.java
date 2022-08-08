package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.client.dto.BasketItemDto;
import org.cryptimeleon.incentive.client.dto.RewardItemDto;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.SpendRequestDto;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.TransactionIdentifier;
import org.cryptimeleon.incentive.promotion.EmptyTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
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
    protected final BasketItemDto firstTestItem = new BasketItemDto("1", "First Test Item", 100);
    protected final BasketItemDto secondTestItem = new BasketItemDto("1", "First Test Item", 100);
    protected final List<BasketItemDto> testBasketItems = List.of(firstTestItem, secondTestItem);

    protected final BasketItemDto basketItemDto = new BasketItemDto("Some ID", "Apple", 1);
    protected final RewardItemDto rewardItemDto = new RewardItemDto(REWARD_ID, "Test Reward Item");

    protected InfoClient infoClient;
    protected BasketClient basketClient;
    protected IncentiveClient incentiveClient;

    protected TestCryptoAssets cryptoAssets;
    protected IncentiveSystem incentiveSystem;


    protected void prepareBasketServiceAndPromotions() {
        infoClient = new InfoClient(infoUrl);
        basketClient = new BasketClient(basketUrl);
        incentiveClient = new IncentiveClient(incentiveUrl);

        cryptoAssets = TestHelper.getCryptoAssets(infoClient, sharedSecret);
        incentiveSystem = new IncentiveSystem(cryptoAssets.getPublicParameters());

        basketClient.newBasketItem(basketItemDto, basketProviderSecret).block();
        basketClient.newRewardItem(rewardItemDto, basketProviderSecret).block();
        basketClient.addShoppingItems(testBasketItems, basketProviderSecret);
        incentiveClient.addPromotions(List.of(testPromotion), incentiveProviderSecret).block();
    }


    /**
     * Performs a full integrated run of the spend-deduct protocol
     * and returns identifying information for the occurred transaction
     * for later use in test cases (e.g. dsprotection integration test).
     *
     * @param token    token spent in the transaction
     * @param basketId ID of the basket used for the basket used in the transaction
     * @return TransactionIdentifier
     */
    protected TransactionIdentifier runSpendDeductWorkflow(Token token, UUID basketId) {
        // generate transaction ID from basket ID
        var tid = cryptoAssets.getPublicParameters().getBg().getZn().createZnElement(new BigInteger(basketId.toString().replace("-", ""), 16));

        // put sample basket item into basket
        basketClient.putItemToBasket(basketId, basketItemDto.getId(), 1).block();

        /*
         * Create spend request and send it.
         * For syntactic reasons, we need to send it as a bulk (consisting of only one request).
         */
        var spendRequest = incentiveSystem.generateSpendRequest(
                testPromotion.getPromotionParameters(),
                token,
                cryptoAssets.getProviderKeyPair().getPk(),
                Vector.of(BigInteger.valueOf(1L)),
                cryptoAssets.getUserKeyPair(),
                tid,
                testTokenUpdate.generateRelationTree(Vector.of(BigInteger.valueOf(1)))
        );
        var spendRequestDto = new SpendRequestDto(
                testPromotion.getPromotionParameters().getPromotionId(),
                testTokenUpdate.getTokenUpdateId(),
                jsonConverter.serialize(spendRequest.getRepresentation()),
                jsonConverter.serialize(new RepresentableRepresentation(new EmptyTokenUpdateMetadata()))
        );
        var bulkRequestDto = new BulkRequestDto(List.of(), List.of(spendRequestDto));
        incentiveClient.sendBulkUpdates(basketId, bulkRequestDto).block();

        // pay basket
        basketClient.payBasket(basketId, paySecret).block();

        // retrieve spend response and handle it
        var bulkResponseDto = incentiveClient.retrieveBulkResults(basketId).block();
        assert bulkResponseDto != null;
        var spendResponseDto = bulkResponseDto.getZkpTokenUpdateResultDtoList().get(0);
        var spendResponse = new SpendResponse(jsonConverter.deserialize(spendResponseDto.getSerializedResponse()), cryptoAssets.getPublicParameters());
        incentiveSystem.handleSpendRequestResponse(
                testPromotion.getPromotionParameters(),
                spendResponse,
                spendRequest,
                token,
                Vector.of(BigInteger.valueOf(1)),
                cryptoAssets.getProviderKeyPair().getPk(),
                cryptoAssets.getUserKeyPair()
        );

        // assemble and return transaction identifier
        var gamma = Util.hashGamma(
                cryptoAssets.getPublicParameters().getBg().getZn(),
                spendRequest.getDsid(),
                tid,
                spendRequest.getCPre0(),
                spendRequest.getCPre1(),
                tid
        );
        return new TransactionIdentifier(tid, gamma);
    }



    /*
     * helper methods
     */

    /**
     * wrapper around crypto. ... .Helper.generateToken that fixes parameters that are the same for all calls anyway.
     *
     * @param promotionParameters promotion parameters for the promotion the token should be used for
     * @param pointVector         point counts in the token
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

    /**
     * Creates a basket and returns its basket ID.
     *
     * @return basket ID
     */
    protected UUID createBasket() {
        return basketClient.createBasket().block();
    }
}
