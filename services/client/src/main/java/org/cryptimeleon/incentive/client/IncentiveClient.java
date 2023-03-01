package org.cryptimeleon.incentive.client;

import org.cryptimeleon.incentive.client.dto.provider.BulkRequestProviderDto;
import org.cryptimeleon.incentive.client.dto.provider.BulkResultsProviderDto;
import org.cryptimeleon.incentive.client.dto.provider.EarnRequestProviderDto;
import org.cryptimeleon.incentive.client.dto.provider.SpendRequestProviderDto;
import org.cryptimeleon.incentive.crypto.model.EarnProviderRequest;
import org.cryptimeleon.incentive.crypto.model.RegistrationCoupon;
import org.cryptimeleon.incentive.crypto.model.SpendProviderRequest;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Client calls for incentive service.
 * Can be used for testing and prototyping.
 */
public class IncentiveClient implements AliveEndpoint {

    /**
     * Webclient configured with the url of the issue service
     */
    private final WebClient incentiveClient;
    private final JSONConverter jsonConverter = new JSONConverter();

    public IncentiveClient(String incentiveServiceUrl) {
        this.incentiveClient = WebClientHelper.buildWebClient(incentiveServiceUrl);
    }

    /**
     * Sends an request to the / endpoint which is configured to return the name of the service
     * This can be used to test whether a service is alive and reachable under some url
     */
    public Mono<String> sendAliveRequest() {
        return incentiveClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Creates a join request.
     *
     * @param serializedJoinRequest the serialized join request
     * @return mono of the server's answer
     */
    public Mono<String> sendJoinRequest(BigInteger promotionId, String serializedJoinRequest) {
        return incentiveClient.post()
                .uri("/join-promotion")
                .header("promotion-id", String.valueOf(promotionId))
                .header("join-request", serializedJoinRequest)
                .retrieve()
                .bodyToMono(String.class);
    }

    public BulkResultsProviderDto sendBulkRequest(BulkRequestProviderDto bulkRequestProviderDto) {
        return incentiveClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestProviderDto))
                .retrieve()
                .bodyToMono(BulkResultsProviderDto.class)
                .block();
    }

    public Mono<ResponseEntity<Void>> addPromotions(List<Promotion> promotions, String providerSecret) {
        return incentiveClient.post()
                .uri("/promotions")
                .header("provider-secret", providerSecret)
                .body(BodyInserters.fromValue(serializedPromotionsRepresentable(promotions)))
                .retrieve()
                .toBodilessEntity();
    }

    private List<String> serializedPromotionsRepresentable(List<Promotion> promotions) {
        return promotions.stream().map(p -> jsonConverter.serialize(new RepresentableRepresentation(p))).collect(Collectors.toList());
    }

    public String registerUserWithCoupon(RegistrationCoupon registrationCoupon) {
        return incentiveClient.get()
                .uri("/register-with-coupon")
                .header("registration-coupon", jsonConverter.serialize(registrationCoupon.getRepresentation()))
                .retrieve()
                .bodyToMono((new ParameterizedTypeReference<String>() {
                }))
                .block();
    }

    public String sendEarnRequest(EarnProviderRequest earnRequest, BigInteger promotionId) {
        var earnRequestDto = new EarnRequestProviderDto(promotionId, jsonConverter.serialize(earnRequest.getRepresentation()));
        var bulkRequest = new BulkRequestProviderDto(
                Collections.emptyList(),
                List.of(earnRequestDto)
        );
        var bulkResponse = sendBulkRequest(bulkRequest);
        var earnResponse = bulkResponse.getEarnResults().get(0);
        return earnResponse.getSerializedEarnResponse();
    }

    public String sendSpendRequest(SpendProviderRequest spendRequest,
                                   BigInteger promotionId,
                                   ZkpTokenUpdateMetadata metadata,
                                   UUID basketId,
                                   UUID tokenUpdateId,
                                   Vector<BigInteger> basketPoints) {
        var spendRequestProviderDto = new SpendRequestProviderDto(promotionId,
                jsonConverter.serialize(spendRequest.getRepresentation()),
                jsonConverter.serialize(new RepresentableRepresentation(metadata)),
                basketId,
                tokenUpdateId,
                basketPoints.toList());
        var bulkRequest = new BulkRequestProviderDto(
                List.of(spendRequestProviderDto),
                Collections.emptyList()
        );
        var bulkResponse = sendBulkRequest(bulkRequest);
        var spendResponse = bulkResponse.getSpendResults().get(0);
        return spendResponse.getSerializedSpendResult();
    }
}
