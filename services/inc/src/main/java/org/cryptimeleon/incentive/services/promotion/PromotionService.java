package org.cryptimeleon.incentive.services.promotion;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.model.EarnRequest;
import org.cryptimeleon.incentive.crypto.model.SpendProviderOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.messages.JoinRequest;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.model.Basket;
import org.cryptimeleon.incentive.promotion.hazel.HazelPromotion;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.services.promotion.repository.BasketRepository;
import org.cryptimeleon.incentive.services.promotion.repository.CryptoRepository;
import org.cryptimeleon.incentive.services.promotion.repository.PromotionRepository;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;


/**
 * This service processes the requests and contains all the business.
 */
@Slf4j
@Service
public class PromotionService {

    private final JSONConverter jsonConverter = new JSONConverter();

    private CryptoRepository cryptoRepository;
    private PromotionRepository promotionRepository;
    private BasketRepository basketRepository;

    @Autowired
    private PromotionService(CryptoRepository cryptoRepository, PromotionRepository promotionRepository, BasketRepository basketRepository) {
        this.cryptoRepository = cryptoRepository;
        this.promotionRepository = promotionRepository;
        this.basketRepository = basketRepository;
    }


    public String[] getPromotions() {
        return promotionRepository.getPromotions().stream()
                .map(Promotion::getRepresentation)
                .map(jsonConverter::serialize)
                .toArray(String[]::new);
    }

    /**
     * Join a promotion with the issue-join protocol.
     *
     * @param promotionId             the id that identifies the promotion
     * @param serializedJoinRequest   the serialized join request
     * @param serializedUserPublicKey the serialized user public key
     * @return a serialized join response
     */
    public String joinPromotion(BigInteger promotionId, String serializedJoinRequest, String serializedUserPublicKey) {
        // Find promotion
        Promotion promotion = promotionRepository.getPromotion(promotionId).orElseThrow(() -> new IncentiveServiceException("Promotion to Join not found!"));

        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();

        UserPublicKey userPublicKey = new UserPublicKey(jsonConverter.deserialize(serializedUserPublicKey), pp.getBg().getG1());
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, providerPublicKey));
        JoinRequest joinRequest = new JoinRequest(jsonConverter.deserialize(serializedJoinRequest), pp, userPublicKey, cwfProofSystem);
        ProviderKeyPair providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        JoinResponse joinResponse = incentiveSystem.generateJoinRequestResponse(promotion.getPromotionParameters(), providerKeyPair, userPublicKey.getUpk(), joinRequest);
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
    public String handleEarnRequest(BigInteger promotionId, String serializedEarnRequest, UUID basketId) {
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

        // TODO: send this to basket server via back channel and return success

        return jsonConverter.serialize(signature.getRepresentation());
    }

    public String handleSpendRequest(BigInteger promotionId, UUID basketId, UUID rewardId, String serializedSpendRequest) {
        log.info("SpendRequest:" + serializedSpendRequest);

        Promotion promotion = promotionRepository.getPromotion(promotionId).orElseThrow(() -> new IncentiveServiceException(String.format("promotionId %d not found", promotionId)));
        ZkpTokenUpdate zkpTokenUpdate = promotion.getRewards().stream().filter(reward1 -> reward1.getTokenUpdateId().equals(rewardId)).findAny().orElseThrow(() -> new IncentiveServiceException("Reward id not found"));

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
        var basketPoints = promotion.computeEarningsForBasket(basket);
        var spendDeductTree = zkpTokenUpdate.generateRelationTree(basketPoints);
        var tid = basket.getBasketId(pp.getBg().getZn());
        FiatShamirProofSystem spendDeductProofSystem = new FiatShamirProofSystem(
                new SpendDeductBooleanZkp(spendDeductTree, pp, promotion.getPromotionParameters(), providerPublicKey)
        );
        var spendRequest = new SpendRequest(jsonConverter.deserialize(serializedSpendRequest), pp, spendDeductProofSystem, tid);

        // Run deduct
        SpendProviderOutput spendProviderOutput = incentiveSystem.generateSpendRequestResponse(promotion.getPromotionParameters(), spendRequest, new ProviderKeyPair(providerSecretKey, providerPublicKey), tid, spendDeductTree);

        // TODO process provider output
        // TODO send result to basket such that it is locked until payment

        return jsonConverter.serialize(spendProviderOutput.getSpendResponse().getRepresentation());
    }

    public void addPromotions(List<String> serializedPromotions) {
        for (String serializedPromotion : serializedPromotions) {
            Promotion promotion = new HazelPromotion(jsonConverter.deserialize(serializedPromotion));
            promotionRepository.addPromotion(promotion);
        }
    }
}
