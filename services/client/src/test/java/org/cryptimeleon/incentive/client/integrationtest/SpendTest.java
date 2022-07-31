package org.cryptimeleon.incentive.client.integrationtest;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.IncentiveClient;
import org.cryptimeleon.incentive.client.InfoClient;
import org.cryptimeleon.incentive.client.dto.BasketItemDto;
import org.cryptimeleon.incentive.client.dto.RewardItemDto;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.SpendRequestDto;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.SpendResponse;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.promotion.EmptyTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.hazel.HazelTokenUpdate;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpendTest extends IncentiveSystemIntegrationTest {
    private final String REWARD_ID = "TEST_REWARD_ID";

    private final JSONConverter jsonConverter = new JSONConverter();

    private final ZkpTokenUpdate testTokenUpdate = new HazelTokenUpdate(
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
    private final BasketItemDto basketItemDto = new BasketItemDto("Some ID", "Apple", 1);
    private final RewardItemDto rewardItemDto = new RewardItemDto(REWARD_ID, "Test Reward Item");

    private InfoClient infoClient;
    private BasketClient basketClient;
    private IncentiveClient incentiveClient;

    private TestCryptoAssets cryptoAssets;
    private IncentiveSystem incentiveSystem;

    @BeforeAll
    protected void prepareBasketAndPromotions() {
        infoClient = new InfoClient(infoUrl);
        basketClient = new BasketClient(basketUrl);
        incentiveClient = new IncentiveClient(incentiveUrl);

        cryptoAssets = TestHelper.getCryptoAssets(infoClient, sharedSecret);
        incentiveSystem = new IncentiveSystem(cryptoAssets.getPublicParameters());

        basketClient.newBasketItem(basketItemDto, basketProviderSecret).block();
        basketClient.newRewardItem(rewardItemDto, basketProviderSecret).block();
        incentiveClient.addPromotions(List.of(testPromotion), incentiveProviderSecret).block();
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

    /**
     * Performs a full integrated run of the spend-deduct protocol
     * and returns the spend request together with the protocol output
     * for further use in the double-spending protection test.
     */
    protected void runSpendDeductWorkflow(Token token, UUID basketId) {
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
    }


    /*
    * helper methods
    */

    /**
     * wrapper around crypto. ... .Helper.generateToken that fixes parameters that are the same for all calls anyway.
     * @param promotionParameters promotion parameters for the promotion the token should be used for
     * @param pointVector point counts in the token
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
     * Creates a basket and returns its basket ID.
     * @return basket ID
     */
    protected UUID createBasket() {
        return basketClient.createBasket().block();
    }
}
