package org.cryptimeleon.incentive.services.issue;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.messages.JoinRequest;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;
import org.cryptimeleon.incentive.crypto.proof.wellformedness.CommitmentWellformednessProtocol;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * This service processes the join requests and contains all the business.
 * Executes the issue logic given a serialized user public key and a serialized join request.
 */
@Service
public class IssueService {

    private CryptoRepository cryptoRepository;

    @Autowired
    private IssueService(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    /**
     * Executes the issue algorithm from the Issue-Join protocol for a given join-request and user public key (both serialized) and returns a serialized join response.
     *
     * @param serializedJoinRequest   the request to process
     * @param serializedUserPublicKey the public key of the other party in the Issue-Join protocol
     * @return the serialized join response
     */
    public String runIssue(String serializedJoinRequest, String serializedUserPublicKey) {
        var pp = cryptoRepository.getPublicParameters();
        var providerPublicKey = cryptoRepository.getProviderPublicKey();
        var providerSecretKey = cryptoRepository.getProviderSecretKey();
        var incentiveSystem = cryptoRepository.getIncentiveSystem();
        // TODO this will be replaced by promotion parameters provided by the promotion service
        var legacyPromotionParameters = incentiveSystem.legacyPromotionParameters();

        JSONConverter jsonConverter = new JSONConverter();

        UserPublicKey userPublicKey = new UserPublicKey(
                jsonConverter.deserialize(serializedUserPublicKey),
                pp.getBg().getG1()
        );
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(
                new CommitmentWellformednessProtocol(pp, providerPublicKey)
        );
        JoinRequest joinRequest = new JoinRequest(
                jsonConverter.deserialize(serializedJoinRequest),
                pp,
                userPublicKey,
                cwfProofSystem
        );
        ProviderKeyPair providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        JoinResponse joinResponse = incentiveSystem.generateJoinRequestResponse(legacyPromotionParameters, providerKeyPair, userPublicKey.getUpk(), joinRequest);
        return jsonConverter.serialize(joinResponse.getRepresentation());
    }
}
