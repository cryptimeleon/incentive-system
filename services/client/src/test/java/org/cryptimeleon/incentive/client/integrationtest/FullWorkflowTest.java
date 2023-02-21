package org.cryptimeleon.incentive.client.integrationtest;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.EarnRequestDto;
import org.cryptimeleon.incentive.client.dto.store.BulkResultsStoreDto;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.promotion.ContextManager;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.model.BasketItem;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
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

        assertThat(incentiveSystem.verifyRegistrationCoupon(registrationCoupon, (s) -> true)).isTrue();

        // Send coupon to provider/incentive service and retrieve SPSEQ
        var serializedRegistrationSignature = incentiveClient.registerUserWithCoupon(registrationCoupon);
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

        Token newToken = runEarnProtocol(token, basket, testPromotion);
        Assertions.assertEquals(newToken.getPoints().map(RingElement::asInteger), testPromotion.computeEarningsForBasket(basket));
    }

    @Test
    @Deprecated
    void earnTestLegacy() {
        var token = generateToken();
        var basket = createBasketWithItems();
        var basketValueForPromotion = testPromotion.computeEarningsForBasket(basket);
        log.info("Run valid credit earn protocol");
        Token newToken = runEarnProtocolLegacy(token, basket, basketValueForPromotion);
        Assertions.assertEquals(newToken.getPoints().map(RingElement::asInteger), basketValueForPromotion);
    }

    @Test
    void spendTest() {
        Token token = generateToken(testPromotion.getPromotionParameters(), Vector.of(BigInteger.valueOf(25)));
        var basket = createBasketWithItems();

        assertThat(cryptoAssets.getPublicParameters().getSpsEq().verify(
                cryptoAssets.getProviderKeyPair().getPk().getPkSpsEq(),
                token.getSignature(),
                token.getCommitment0(),
                cryptoAssets.getPublicParameters().getG1Generator(),
                cryptoAssets.getPublicParameters().getG1Generator().pow(testPromotion.getPromotionParameters().getPromotionId())
        )).isTrue();

        var updatedToken = runSpendProtocol(token, basket, testPromotion, testTokenUpdate);

        var basketAfterSpend = basketClient.getBasket(basket.getBasketId()).block();
        assert basketAfterSpend != null;
        org.assertj.core.api.Assertions.assertThat(basketAfterSpend.getRewardItems()).hasSize(1).allMatch(rewardItemDto -> rewardItemDto.getId().equals(REWARD_ID));
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

    private Token runEarnProtocol(Token token, org.cryptimeleon.incentive.promotion.model.Basket basket, Promotion promotion) {
        var pointsToEarn = promotion.computeEarningsForBasket(basket);

        // Communication with store
        // Send request
        var earnCouponRequest = incentiveSystem.generateEarnCouponRequest(token, cryptoAssets.getUserKeyPair());
        assertThat(basketClient.sendEarn(basket.getBasketId(), promotion.getPromotionParameters().getPromotionId(), earnCouponRequest).getStatusCode().is2xxSuccessful())
                .isTrue();

        // Pay basket
        basketClient.payBasket(basket.getBasketId(), paySecret);

        // Obtain response
        var batchResponse = basketClient.retrieveBulkResponse(basket.getBasketId());
        var earnCoupon = new EarnStoreCouponSignature(jsonConverter.deserialize(batchResponse.getEarnResults().get(0).getSerializedEarnCouponSignature()));
        assertThat(incentiveSystem.verifyEarnCoupon(earnCouponRequest, promotion.getPromotionParameters().getPromotionId(), pointsToEarn, earnCoupon, storePublicKey -> true))
                .isTrue();

        // Communication with provider
        var earnRequest = incentiveSystem.generateEarnRequest(token, cryptoAssets.getProviderKeyPair().getPk(), cryptoAssets.getUserKeyPair(), pointsToEarn, earnCoupon);
        var serializedEarnResponse = incentiveClient.sendEarnRequest(earnRequest, promotion.getPromotionParameters().getPromotionId());
        SPSEQSignature updatedSignature = new SPSEQSignature(jsonConverter.deserialize(serializedEarnResponse), cryptoAssets.getPublicParameters().getBg().getG1(), cryptoAssets.getPublicParameters().getBg().getG2());
        return incentiveSystem.handleEarnResponse(earnRequest, updatedSignature, promotion.getPromotionParameters(), token, cryptoAssets.getUserKeyPair(), cryptoAssets.getProviderKeyPair().getPk());
    }

    private Token runSpendProtocol(Token token, Basket basket, Promotion promotion, ZkpTokenUpdate tokenUpdate) {
        // Prepare request
        ZkpTokenUpdateMetadata metadata = promotion.generateMetadataForUpdate();
        Vector<BigInteger> basketPoints = promotion.computeEarningsForBasket(basket);
        Vector<BigInteger> pointsAfterSpend = tokenUpdate.computeSatisfyingNewPointsVector(token.getPoints().map(RingElement::asInteger), basketPoints, metadata).get();
        SpendDeductTree tree = tokenUpdate.generateRelationTree(basketPoints);
        UniqueByteRepresentable context = ContextManager.computeContext(tokenUpdate.getTokenUpdateId(), basketPoints, metadata);
        SpendCouponRequest spendStoreRequest = incentiveSystem.generateStoreSpendRequest(cryptoAssets.getUserKeyPair(), cryptoAssets.getProviderKeyPair().getPk(), token, promotion.getPromotionParameters(), basket.getBasketId(), pointsAfterSpend, tree, context);

        // Send request
        assertThat(basketClient.sendSpend(basket.getBasketId(), promotion.getPromotionParameters().getPromotionId(), tokenUpdate.getTokenUpdateId(), spendStoreRequest, metadata).getStatusCode().is2xxSuccessful())
                .isTrue();

        // Pay basket
        basketClient.payBasket(basket.getBasketId(), paySecret);

        // Obtain response
        BulkResultsStoreDto batchResponse = basketClient.retrieveBulkResponse(basket.getBasketId());
        SpendCouponSignature spendCouponSignature = new SpendCouponSignature(jsonConverter.deserialize(batchResponse.getSpendResults().get(0).getSerializedSpendCouponSignature()));
        assertThat(incentiveSystem.verifySpendCouponSignature(spendStoreRequest, spendCouponSignature, promotion.getPromotionParameters(), basket.getBasketId()))
                .isTrue();

        SpendRequestECDSA spendRequestECDSA = new SpendRequestECDSA(spendStoreRequest, spendCouponSignature);
        String serializedSpendResponse = incentiveClient.sendSpendRequest(spendRequestECDSA, promotion.getPromotionParameters().getPromotionId(), metadata, basket.getBasketId(), tokenUpdate.getTokenUpdateId(), basketPoints);
        SpendResponseECDSA spendResponseECDSA = new SpendResponseECDSA(jsonConverter.deserialize(serializedSpendResponse), cryptoAssets.getPublicParameters());
        return incentiveSystem.retrieveUpdatedTokenFromSpendResponse(cryptoAssets.getUserKeyPair(), cryptoAssets.getProviderKeyPair().getPk(), token, promotion.getPromotionParameters(), pointsAfterSpend, spendRequestECDSA, spendResponseECDSA);
    }

    @Deprecated
    private Token runEarnProtocolLegacy(Token token, org.cryptimeleon.incentive.promotion.model.Basket basket, org.cryptimeleon.math.structures.cartesian.Vector<java.math.BigInteger> basketValueForPromotion) {
        var earnRequest = incentiveSystem.generateEarnRequest(token, cryptoAssets.getProviderKeyPair().getPk(), cryptoAssets.getUserKeyPair());
        var serializedEarnRequest = jsonConverter.serialize(earnRequest.getRepresentation());
        incentiveClient.sendBulkUpdates(basket.getBasketId(), new BulkRequestDto(List.of(new EarnRequestDto(testPromotion.getPromotionParameters().getPromotionId(), serializedEarnRequest)), Collections.emptyList())).block();
        basketClient.payBasket(basket.getBasketId(), paySecret);
        var serializedSignature = incentiveClient.retrieveBulkResults(basket.getBasketId()).block().getEarnTokenUpdateResultDtoList().get(0).getSerializedEarnResponse();
        var signature = new SPSEQSignature(jsonConverter.deserialize(serializedSignature), cryptoAssets.getPublicParameters().getBg().getG1(), cryptoAssets.getPublicParameters().getBg().getG2());
        return incentiveSystem.handleEarnRequestResponse(testPromotion.getPromotionParameters(), earnRequest, signature, basketValueForPromotion, token, cryptoAssets.getProviderKeyPair().getPk(), cryptoAssets.getUserKeyPair());
    }
}
