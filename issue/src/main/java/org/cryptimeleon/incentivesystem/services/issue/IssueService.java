package org.cryptimeleon.incentivesystem.services.issue;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentivesystem.client.InfoClient;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinResponse;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentivesystem.services.issue.model.JoinRequestDTO;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class IssueService {
    @Value("${info-service-url}")
    private String infoServiceUrl;
    @Value("${provider.shared-secret}")
    private String sharedSecret;
    private static final int MAX_TRIES = 10;
    private IncentiveSystem incentiveSystem;
    private IncentivePublicParameters pp;
    private ProviderSecretKey providerSecretKey;
    private ProviderPublicKey providerPublicKey;


    Logger logger = LoggerFactory.getLogger(IssueService.class);

    @PostConstruct
    public void init() {
        logger.info("Querying configuration from info service");
        logger.info(sharedSecret);

        InfoClient infoClient = new InfoClient(infoServiceUrl);
        JSONConverter jsonConverter = new JSONConverter();

        for (int i = 0; i < MAX_TRIES; i++) {
            try {
                logger.info("Try: " + i);
                String serializedPublicParameters = infoClient.querySerializedPublicParameters().block();
                String serializedProviderPublicKey = infoClient.querySerializedProviderPublicKey().block();
                String serializedProviderSecretKey = infoClient.querySerializedProviderSecretKey(sharedSecret).block();
                this.pp = new IncentivePublicParameters(jsonConverter.deserialize(serializedPublicParameters));
                this.providerPublicKey = new ProviderPublicKey(jsonConverter.deserialize(serializedProviderPublicKey), pp.getSpsEq(), pp.getBg().getG1());
                this.providerSecretKey = new ProviderSecretKey(jsonConverter.deserialize(serializedProviderSecretKey), pp.getSpsEq(), pp.getBg().getZn(), pp.getPrfToZn());
                this.incentiveSystem = new IncentiveSystem(pp);
                break;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep((long) (1000 * Math.pow(2, i)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String runIssueJoinProtocol(JoinRequestDTO joinRequestDTO) {
        JSONConverter jsonConverter = new JSONConverter();
        UserPublicKey userPublicKey = new UserPublicKey(jsonConverter.deserialize(joinRequestDTO.getSerializedUserPublicKey()), pp.getBg().getG1());
        FiatShamirProofSystem cwfProofSystem = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, providerPublicKey));
        JoinRequest joinRequest = new JoinRequest(jsonConverter.deserialize(joinRequestDTO.getSerializedJoinRequest()), pp, userPublicKey, cwfProofSystem);
        ProviderKeyPair providerKeyPair = new ProviderKeyPair(providerSecretKey, providerPublicKey);
        JoinResponse joinResponse = incentiveSystem.generateJoinRequestResponse(pp, providerKeyPair, userPublicKey.getUpk(), joinRequest);
        return jsonConverter.serialize(joinResponse.getRepresentation());
    }
}
