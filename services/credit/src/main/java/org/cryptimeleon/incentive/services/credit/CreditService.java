package org.cryptimeleon.incentive.services.credit;

import org.cryptimeleon.incentive.crypto.model.EarnRequest;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.services.credit.exception.IncentiveException;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.UUID;

/**
 * This service contains the business logic of the credit-earn protocol.
 */
@Service
public class CreditService {

    private Logger logger = LoggerFactory.getLogger(CreditService.class);
    private BasketRepository basketRepository;
    private CryptoRepository cryptoRepository;

    @Autowired
    public CreditService(BasketRepository basketRepository, CryptoRepository cryptoRepository) {
        this.basketRepository = basketRepository;
        this.cryptoRepository = cryptoRepository;
    }

    /**
     * Verify and run credit-earn protocol.
     * Communicates with basket server to ensure the request is valid.
     *
     * @param serializedEarnRequest the earn request to process
     * @param basketId              id of the basket that is used for this earn protocol run
     * @return serialized signature
     */
    public String handleEarnRequest(String serializedEarnRequest, UUID basketId) {
        logger.info("EarnRequest:" + serializedEarnRequest);

        // Validations
        var basket = basketRepository.getBasket(basketId);
        logger.info("Queried basket:" + basket);
        if (!basket.isPaid()) {
            throw new IncentiveException("Basket not paid");
        }
        if (basket.isRedeemed() && !basket.getRedeemRequest().equals(serializedEarnRequest)) {
            throw new IncentiveException("Basket was redeemed with another request!");
        }

        if (!basket.isRedeemed()) {
            basketRepository.redeem(basketId, serializedEarnRequest, basket.getValue());
            /*
             * TODO think about when to redeem
             * Maybe add some kind of lock mechanism that only sends the basket to redeemed after the response was generated
             */
        }

        // Prepare incentive system
        var k = BigInteger.valueOf(basket.getValue());
        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();
        var jsonConverter = new JSONConverter();

        // Run server part of protocol and serialize signature
        var earnRequest = new EarnRequest(jsonConverter.deserialize(serializedEarnRequest), pp);
        var providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        var signature = incentiveSystem.generateEarnRequestResponse(earnRequest, k, providerKeyPair);
        return jsonConverter.serialize(signature.getRepresentation());
    }
}
