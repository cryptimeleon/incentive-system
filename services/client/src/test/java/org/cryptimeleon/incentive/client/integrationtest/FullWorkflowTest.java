package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.EarnRequestDto;
import org.cryptimeleon.incentive.crypto.model.JoinResponse;
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test a full (correct) protocol flow.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FullWorkflowTest extends TransactionTestPreparation {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FullWorkflowTest.class);

    @BeforeAll
    void setup() {
        prepareBasketServiceAndPromotions();
    }

    @Test
    void registrationTest() {
        var serializedUserPublicKey = jsonConverter.serialize(cryptoAssets.getUserKeyPair().getPk().getRepresentation());

        // Send request to store/basket service and retrieve coupon
        var serializedRegistrationCoupon = basketClient.registerUser(serializedUserPublicKey, "Some User Name");
        var registrationCoupon = new RegistrationCoupon(jsonConverter.deserialize(serializedRegistrationCoupon), incentiveRestorer);

        assertThat(incentiveSystem.verifyRegistrationCoupon(registrationCoupon, (s)->true)).isTrue();

        // Send coupon to provider/incentive service and retrieve SPSEQ
        var serializedRegistrationSignature= incentiveClient.registerUserWithCoupon(registrationCoupon);
        var registrationSignature = new SPSEQSignature(jsonConverter.deserialize(serializedRegistrationSignature), cryptoAssets.getPublicParameters().getBg().getG1(), cryptoAssets.getPublicParameters().getBg().getG2());

        assertThat(incentiveSystem.verifyRegistrationToken(cryptoAssets.getProviderKeyPair().getPk(), registrationSignature, registrationCoupon)).isTrue();
    }

    @Test
    void joinTest() {
        Token token = joinPromotion();
        var zeroPointsVector = RingElementVector.generate(cryptoAssets.getPublicParameters().getBg().getZn()::getZeroElement, testPromotion.getPromotionParameters().getPointsVectorSize());
        assertThat(token.getPoints()).isEqualTo(zeroPointsVector);
    }

    @Test
    void earnTest() {
        var token = generateToken();
        var basket = createBasketWithItems();
        var basketValueForPromotion = testPromotion.computeEarningsForBasket(basket);
        log.info("Run valid credit earn protocol");
        Token newToken = runEarnProtocol(token, basket, basketValueForPromotion);
        Assertions.assertEquals(newToken.getPoints().map(RingElement::asInteger), basketValueForPromotion);
    }

    @Test
    void spendRewardsAddedToBasketTest() {
        Token token = generateToken(testPromotion.getPromotionParameters(), Vector.of(BigInteger.valueOf(20)));
        var basketId = createBasket();
        assert basketId != null;
        log.info("BasketId: " + basketId);
        runSpendDeductWorkflow(token, basketId);
        var basketAfterSpend = basketClient.getBasket(basketId).block();
        assert basketAfterSpend != null;
        org.assertj.core.api.Assertions.assertThat(basketAfterSpend.getRewardItems()).hasSize(1).allMatch(rewardItemDto -> rewardItemDto.getId().equals(REWARD_ID));
    }

    private Basket createBasketWithItems() {
        UUID basketId = basketClient.createBasket().block();
        basketClient.putItemToBasket(basketId, firstTestItem.getId(), 3).block();
        basketClient.putItemToBasket(basketId, secondTestItem.getId(), 1).block();
        var basketDto = basketClient.getBasket(basketId).block();
        assertThat(basketDto).isNotNull();
        return new Basket(basketDto.getBasketID(), basketDto.getBasketItems().stream().map(i -> new BasketItem(i.getId(), i.getTitle(), i.getPrice(), i.getCount())).collect(Collectors.toList()));
    }

    private Token joinPromotion() {
        var joinTuple = incentiveSystem.generateJoinRequest(cryptoAssets.getProviderKeyPair().getPk(), cryptoAssets.getUserKeyPair());
        var serializedJoinResponse = incentiveClient.sendJoinRequest(testPromotion.getPromotionParameters().getPromotionId(), jsonConverter.serialize(joinTuple.getJoinRequest().getRepresentation())).block(Duration.ofSeconds(1));
        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), cryptoAssets.getPublicParameters());
        return incentiveSystem.handleJoinRequestResponse(testPromotion.getPromotionParameters(), cryptoAssets.getProviderKeyPair().getPk(), joinTuple, joinResponse);
    }

    private Token runEarnProtocol(Token token, org.cryptimeleon.incentive.promotion.model.Basket basket, org.cryptimeleon.math.structures.cartesian.Vector<java.math.BigInteger> basketValueForPromotion) {
        var earnRequest = incentiveSystem.generateEarnRequest(token, cryptoAssets.getProviderKeyPair().getPk(), cryptoAssets.getUserKeyPair());
        var serializedEarnRequest = jsonConverter.serialize(earnRequest.getRepresentation());
        incentiveClient.sendBulkUpdates(basket.getBasketId(), new BulkRequestDto(List.of(new EarnRequestDto(testPromotion.getPromotionParameters().getPromotionId(), serializedEarnRequest)), Collections.emptyList())).block();
        basketClient.payBasket(basket.getBasketId(), basket.computeBasketValue(), paySecret).block();
        var serializedSignature = incentiveClient.retrieveBulkResults(basket.getBasketId()).block().getEarnTokenUpdateResultDtoList().get(0).getSerializedEarnResponse();
        var signature = new SPSEQSignature(jsonConverter.deserialize(serializedSignature), cryptoAssets.getPublicParameters().getBg().getG1(), cryptoAssets.getPublicParameters().getBg().getG2());
        return incentiveSystem.handleEarnRequestResponse(testPromotion.getPromotionParameters(), earnRequest, signature, basketValueForPromotion, token, cryptoAssets.getProviderKeyPair().getPk(), cryptoAssets.getUserKeyPair());
    }
}
