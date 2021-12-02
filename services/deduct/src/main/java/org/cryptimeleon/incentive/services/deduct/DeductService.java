package org.cryptimeleon.incentive.services.deduct;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.client.BasketClient;
import org.cryptimeleon.incentive.client.DSProtectionClient;
import org.cryptimeleon.incentive.crypto.model.DeductOutput;
import org.cryptimeleon.incentive.crypto.model.SpendRequest;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.proof.spend.leaf.TokenUpdateLeaf;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.TokenPointsZkp;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.UUID;

/**
 * This service processes the spend requests and contains all business logic.
 * Executes the Deduct logic (see 2020 inc sys paper) given a serialized user public key and a serialized spend request.
 */
@Service
public class DeductService {
    private CryptoRepository cryptoRepository; // encapsulates all crypto assets the provider needs to provide the deduct service, set via dependency injection ("autowired") mechanism of Spring Boot

    private DSProtectionClient dsProtectionClient; // reference to the object making the queries to the double-spending protection service, set via dependency injection ("autowired") mechanism of Spring Boot

    private BasketClient basketClient; // reference to the object making the queries to the basket service, set via dependency injection mechanism ("autowired") mechanism of Spring Boot

    // TODO: hard-coded values, obtain actual amounts from promotion api once its done
    private final BigInteger POINTS_REQUIRED = new BigInteger("231");
    private final BigInteger TRANSACTION_ID = new BigInteger("445");

    /**
     * Default constructor to be executed when an object of this class is used as a Spring bean.
     */
    @Autowired
    private DeductService(CryptoRepository cr, DSProtectionClient dspc) {
        this.cryptoRepository = cr;
        this.dsProtectionClient = dspc;
    }

    /**
     * Executes the Deduct algorithm from the Spend-Deduct protocol for a given spend request and a user public key (both serialized) and returns a serialized spend response.
     * @param serializedSpendRequest the request to process (as a serialized Representation object)
     * @param basketID id of the users basket. Needed to deduct previously collected points from token.
     * @return the serialized spend response
     */
    // TODO: add a promotion identifier or something similar as parameter (need to find out which reward item to add to the basket if any)
    public String runDeduct(String serializedSpendRequest, UUID basketID) {
        // TODO: add all basket-related logic (querying k and tid from basket server using basketID); requires implementation of new endpoints in basket service
        // retrieve serialized crypto assets from the crypto repository
        var pp = cryptoRepository.getPp(); // for shorthand purposes
        var providerPublicKey = cryptoRepository.getPk();
        var providerSecretKey = cryptoRepository.getSk();
        var providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        var incentiveSystem = cryptoRepository.getIncentiveSystem();

        // creating hard-coded transactionId and spend amount TODO: remove once basket server endpoints are implemented
        Zn.ZnElement transactionId = pp.getBg().getZn().createZnElement(TRANSACTION_ID);
        BigInteger spendAmount = new BigInteger(POINTS_REQUIRED.toString());

        // deserializing assets and wrapping up parameters for processing the request
        JSONConverter jsonConverter = new JSONConverter();
        SpendDeductTree legacyLeaf = new TokenUpdateLeaf(
                "legacyLeaf",
                new Vector<BigInteger>(spendAmount),
                null,
                new Vector<BigInteger>(BigInteger.ONE),
                new Vector<BigInteger>(BigInteger.ZERO)
        );
        FiatShamirProofSystem spendDeductZkpProofSystem = new FiatShamirProofSystem( // proof system needed for constructing the spend request from a representation
                new TokenPointsZkp(
                        pp,
                        new Vector<BigInteger>(spendAmount),
                        null,
                        providerPublicKey,
                        incentiveSystem.legacyPromotionParameters()
                )
        );
        SpendRequest spendRequest = new SpendRequest(
                jsonConverter.deserialize(serializedSpendRequest),
                pp,
                spendDeductZkpProofSystem,
                transactionId
        );

        // executing Deduct (i.e. processing the spend request)
        DeductOutput deductOutput = incentiveSystem.generateSpendRequestResponse(
                incentiveSystem.legacyPromotionParameters(),
                spendRequest,
                providerKeyPair,
                transactionId,
                new SpendDeductBooleanZkp(
                        legacyLeaf,
                        pp,
                        incentiveSystem.legacyPromotionParameters(),
                        providerPublicKey
                )
        );

        // adding the generated transaction data to the double-spending database
        /** this.dsProtectionClient.dbSync(
                transactionId,
                spendRequest.getDsid(),
                deductOutput.getDstag(),
                spendAmount
        ); */ // TODO: uncomment for production and system test

        // TODO: need to cache parameter tuple and only periodically send tuples over to dsprot. via dbsync

        /**
         * TODO: interaction with basket and promotion service to add a special item to the basket and check whether user can afford reward ("this item certifies that the user receives a teddy bear")
         * for now (end-of-february version), only one promotion type is planned.
         * (i.e. shop assistant will hand out teddy bear to customer spending 2000p).
         * However, for further type of promotions (-> future work), the deduct service might need further communication with the basket service here
         * (think of promotions like "pay 100p to get 1€ off for the total price of your basket" or
         * "pay 100p to get a glass of hazelnut spread with 50% off")
         * TODO: this comprises finding out which special item to add; passed promotion ID can be resolved by communicating with the promotion service (yet to be implemented)
         */

        // extract and return spend response
        return jsonConverter.serialize(deductOutput.getSpendResponse().getRepresentation());
    }
}
