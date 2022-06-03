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
    private final Promotion testPromotion = new HazelPromotion(
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
    void prepareBasketAndPromotions() {
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
        Token token = Helper.generateToken(cryptoAssets.getPublicParameters(),
                cryptoAssets.getUserKeyPair(),
                cryptoAssets.getProviderKeyPair(),
                testPromotion.getPromotionParameters(),
                Vector.of(BigInteger.valueOf(20))
        );
        var basketId = basketClient.createBasket().block();
        assert basketId != null;
        log.info("BasketId" + basketId.toString());

        runSpendWorkflow(token, basketId);
        var basketAfterSpend = basketClient.getBasket(basketId).block();

        assert basketAfterSpend != null;
        assertThat(basketAfterSpend.getRewardItems()).containsExactly(REWARD_ID);
    }

    private void runSpendWorkflow(Token token, UUID basketId) {
        var tid = cryptoAssets.getPublicParameters().getBg().getZn().createZnElement(new BigInteger(basketId.toString().replace("-", ""), 16));
        basketClient.putItemToBasket(basketId, basketItemDto.getId(), 1).block();
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
                jsonConverter.serialize(new RepresentableRepresentation(new EmptyTokenUpdateMetadata())));
        var bulkRequestDto = new BulkRequestDto(List.of(), List.of(spendRequestDto));
        incentiveClient.sendBulkUpdates(basketId, bulkRequestDto).block();
        basketClient.payBasket(basketId, paySecret).block();
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
                cryptoAssets.getUserKeyPair());
    }
}