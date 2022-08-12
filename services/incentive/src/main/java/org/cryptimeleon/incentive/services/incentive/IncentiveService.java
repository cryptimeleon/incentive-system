package org.cryptimeleon.incentive.services.incentive;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSigningKey;
import org.cryptimeleon.incentive.client.dto.inc.*;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.sideeffect.RewardSideEffect;
import org.cryptimeleon.incentive.promotion.sideeffect.SideEffect;
import org.cryptimeleon.incentive.services.incentive.repository.*;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * This service processes the requests and contains all the business.
 */
@Slf4j
@Service
public class IncentiveService {
    private final JSONConverter jsonConverter = new JSONConverter();

    private final CryptoRepository cryptoRepository;
    private final PromotionRepository promotionRepository;
    private final BasketRepository basketRepository;
    private final TokenUpdateResultRepository tokenUpdateResultRepository;
    private final OfflineDSPRepository offlineDspRepository;

    @Autowired
    private IncentiveService(
            CryptoRepository cryptoRepository,
            PromotionRepository promotionRepository,
            BasketRepository basketRepository,
            TokenUpdateResultRepository tokenUpdateResultRepository,
            OfflineDSPRepository offlineDspRepository
    ) {
        this.cryptoRepository = cryptoRepository;
        this.promotionRepository = promotionRepository;
        this.basketRepository = basketRepository;
        this.tokenUpdateResultRepository = tokenUpdateResultRepository;
        this.offlineDspRepository = offlineDspRepository;
    }


    public String[] getPromotions() {
        return promotionRepository.getPromotions().stream()
                .map(RepresentableRepresentation::new)
                .map(jsonConverter::serialize)
                .toArray(String[]::new);
    }

    /**
     * Join a promotion with the issue-join protocol.
     *
     * @param promotionId           the id that identifies the promotion
     * @param serializedJoinRequest the serialized join request
     * @return a serialized join response
     */
    public String joinPromotion(BigInteger promotionId, String serializedJoinRequest) {
        // Find promotion
        Promotion promotion = promotionRepository.getPromotion(promotionId).orElseThrow(() -> new IncentiveServiceException("Promotion to Join not found!"));

        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();

        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, providerPublicKey));
        JoinRequest joinRequest = new JoinRequest(jsonConverter.deserialize(serializedJoinRequest), pp, cwfProofSystem);
        ProviderKeyPair providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        JoinResponse joinResponse = incentiveSystem.generateJoinRequestResponse(promotion.getPromotionParameters(), providerKeyPair, joinRequest);
        return jsonConverter.serialize(joinResponse.getRepresentation());
    }


    /**
     * Verify and run credit-earn protocol.
     * Communicates with basket server to ensure the request is valid.
     *
     * @param serializedEarnRequest the earn request to process
     * @param basketId              id of the basket that is used for this earn protocol run
     * @return serialized signature
     */
    private String handleEarnRequest(BigInteger promotionId, String serializedEarnRequest, UUID basketId) {
        log.info("EarnRequest:" + serializedEarnRequest);

        Promotion promotion = promotionRepository.getPromotion(promotionId).orElseThrow(() -> new IncentiveServiceException(String.format("promotionId %d not found", promotionId)));

        // Validations
        Basket basket = basketRepository.getBasket(basketId);
        if (basket == null) throw new IncentiveServiceException("Basket not found!");
        log.info("Queried user basket " + basket.toString());

        // TODO this basket api will change, how about storing a hash of the request only?
        // TODO sanity checks on basket, wait for new api

        Vector<BigInteger> pointsToEarn = promotion.computeEarningsForBasket(basket);

        // Prepare incentive system
        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();

        // Run server part of protocol and serialize signature
        var earnRequest = new EarnRequest(jsonConverter.deserialize(serializedEarnRequest), pp);
        var providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        var signature = incentiveSystem.generateEarnRequestResponse(promotion.getPromotionParameters(), earnRequest, pointsToEarn, providerKeyPair);

        return jsonConverter.serialize(signature.getRepresentation());
    }

    private SideEffect handleSpendRequest(BigInteger promotionId, UUID basketId, UUID rewardId, String serializedSpendRequest, String serializedMetadata) {
        log.info("SpendRequest:" + serializedSpendRequest);

        Promotion promotion = promotionRepository.getPromotion(promotionId).orElseThrow(() -> new IncentiveServiceException(String.format("promotionId %d not found", promotionId)));
        ZkpTokenUpdate zkpTokenUpdate = promotion.getZkpTokenUpdates().stream().filter(reward1 -> reward1.getTokenUpdateId().equals(rewardId)).findAny().orElseThrow(() -> new IncentiveServiceException("Reward id not found"));

        // Prepare incentive system
        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();

        Basket basket = basketRepository.getBasket(basketId);
        if (basket == null) throw new IncentiveServiceException("Basket not found!");
        log.info("Queried user basket " + basket.toString());
        // TODO some sanity checks on basket, wait for new basket service api

        // Prepare zkp
        var metadata = (ZkpTokenUpdateMetadata) ((RepresentableRepresentation) jsonConverter.deserialize(serializedMetadata)).recreateRepresentable();
        if (!zkpTokenUpdate.validateTokenUpdateMetadata(metadata)) {
            throw new RuntimeException("Metadata is invalid for zkpTokenUpdate!");
        }

        var basketPoints = promotion.computeEarningsForBasket(basket);
        var spendDeductTree = zkpTokenUpdate.generateRelationTree(basketPoints, metadata);
        var tid = basket.getBasketId(pp.getBg().getZn());
        FiatShamirProofSystem spendDeductProofSystem = new FiatShamirProofSystem(
                new SpendDeductBooleanZkp(spendDeductTree, pp, promotion.getPromotionParameters(), providerPublicKey)
        );
        var spendRequest = new SpendRequest(jsonConverter.deserialize(serializedSpendRequest), pp, spendDeductProofSystem, tid, tid);

        // Run deduct
        // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
        DeductOutput spendProviderOutput = incentiveSystem.generateSpendRequestResponse(promotion.getPromotionParameters(), spendRequest, new ProviderKeyPair(providerSecretKey, providerPublicKey), tid, spendDeductTree, tid);

        offlineDspRepository.addToDbSyncQueue(promotionId, tid, spendRequest, spendProviderOutput);

        var result = jsonConverter.serialize(spendProviderOutput.getSpendResponse().getRepresentation());
        log.info("SpendResult: " + result);
        tokenUpdateResultRepository.insertZkpTokenUpdateResponse(basketId, promotionId, zkpTokenUpdate.getTokenUpdateId(), result);

        return zkpTokenUpdate.getSideEffect();
    }

    public void addPromotions(List<String> serializedPromotions) {
        for (String serializedPromotion : serializedPromotions) {
            Promotion promotion = recreatePromotionFromRepresentation(serializedPromotion);
            promotionRepository.addPromotion(promotion);
        }
    }

    private Promotion recreatePromotionFromRepresentation(String serializedPromotion) {
        RepresentableRepresentation representableRepresentation = (RepresentableRepresentation) jsonConverter.deserialize(serializedPromotion);
        return (Promotion) representableRepresentation.recreateRepresentable();
    }

    public void deleteAllPromotions() {
        promotionRepository.deleteAllPromotions();
    }

    public void handleBulk(UUID basketId, BulkRequestDto bulkRequestDto) {
        // Can only perform zkp updates on baskets that are locked but not paid.
        basketRepository.lockBasket(basketId);
        if (basketRepository.isBasketPaid(basketId)) {
            throw new RuntimeException("Basket already paid!");
        }

        log.info("Start bulk proofs");
        var rewardIds = new ArrayList<String>();
        for (SpendRequestDto spendRequestDto : bulkRequestDto.getSpendRequestDtoList()) {
            var sideEffect = handleSpendRequest(spendRequestDto.getPromotionId(), basketId, spendRequestDto.getTokenUpdateId(), spendRequestDto.getSerializedSpendRequest(), spendRequestDto.getSerializedMetadata());
            if (sideEffect instanceof RewardSideEffect) {
                rewardIds.add(((RewardSideEffect) sideEffect).getRewardId());
            }
        }
        basketRepository.setRewardsOfBasket(basketId, rewardIds);
        for (EarnRequestDto earnRequestDto : bulkRequestDto.getEarnRequestDtoList()) {
            var result = handleEarnRequest(earnRequestDto.getPromotionId(), earnRequestDto.getSerializedEarnRequest(), basketId);
            log.info("EarnResult: " + result);
            tokenUpdateResultRepository.insertEarnResponse(basketId, earnRequestDto.getPromotionId(), result);
        }
        log.info("Bulk proofs for basket " + basketId.toString() + " finished!");
    }

    public TokenUpdateResultsDto retrieveBulkResults(UUID basketId) {
        if (!basketRepository.isBasketPaid(basketId)) {
            throw new BasketNotPaidException();
        }
        var results = tokenUpdateResultRepository.getUpdateResults(basketId).values();
        log.info(String.valueOf(results));
        return new TokenUpdateResultsDto(
                results.stream().filter(tokenUpdateResult -> tokenUpdateResult instanceof ZkpTokenUpdateResultDto).map(i -> (ZkpTokenUpdateResultDto) i).collect(Collectors.toList()),
                results.stream().filter(tokenUpdateResult -> tokenUpdateResult instanceof EarnTokenUpdateResultDto).map(i -> (EarnTokenUpdateResultDto) i).collect(Collectors.toList())
        );
    }

    public String generateGenesisSignature(String serializedUserPublicKey) {
        var pp = cryptoRepository.getPublicParameters();
        var sk = cryptoRepository.getProviderSecretKey().getGenesisSpsEqSk();

        var upk = pp.getBg().getG1().restoreElement(jsonConverter.deserialize(serializedUserPublicKey));

        SPSEQSignature signature = generateGenesisSignature(pp, sk, upk);

        return jsonConverter.serialize(signature.getRepresentation());
    }

    private SPSEQSignature generateGenesisSignature(IncentivePublicParameters pp, SPSEQSigningKey sk, GroupElement upk) {
        return (SPSEQSignature) pp.getSpsEq().sign(
                sk,
                upk,
                pp.getW()
        );
    }
}