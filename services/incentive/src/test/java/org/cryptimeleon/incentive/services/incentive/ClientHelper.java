package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.provider.BulkRequestProviderDto;
import org.cryptimeleon.incentive.client.dto.provider.BulkResultsProviderDto;
import org.cryptimeleon.incentive.client.dto.provider.EarnRequestProviderDto;
import org.cryptimeleon.incentive.client.dto.provider.SpendRequestProviderDto;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.promotion.Promotion;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdate;
import org.cryptimeleon.incentive.promotion.ZkpTokenUpdateMetadata;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements the functionality of the incentive client for the test cases.
 * <p>
 * More precisely, a WebTestClient is used in the test cases
 * so this class provides one method for each incentive service endpoint
 * which makes the passed WebTestClient make the respective request.
 */
public class ClientHelper {

    private static final JSONConverter jsonConverter = new JSONConverter();

    /**
     * Wrapper for request to add-promotion endpoint of incentive server.
     *
     * @param webClient      test client used to make request to server
     * @param promotionToAdd promotion object
     * @param providerSecret shared secret used to authenticate request
     * @param expectedStatus if another HTTP status than this is sent back for the query, an exception is thrown
     */
    static void addPromotion(WebTestClient webClient, Promotion promotionToAdd, String providerSecret, HttpStatus expectedStatus) {
        webClient.post()
                .uri("/promotions")
                .header("provider-secret", providerSecret)
                .body(BodyInserters.fromValue(
                        serializePromotionsWithType(promotionToAdd)
                ))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    /**
     * Wrapper around GET request for listing all promotions in the system.
     * Note that this request does not need to be authenticated
     * because it does neither change the server state nor reveals confidential data.
     *
     * @param webClient test client used to make request to the server
     * @return list of promotion objects
     */
    static List<Promotion> getPromotions(@Autowired WebTestClient webClient) {
        // retrieve promotions from server
        String[] newSerializedPromotions = webClient
                .get()
                .uri("/promotions")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String[].class)
                .returnResult()
                .getResponseBody();

        // make sure that formatting is fine (i.e. valid string array is returned)
        assert newSerializedPromotions != null;

        // type conversion
        return Arrays.stream(newSerializedPromotions)
                .map(s -> (Promotion) ((RepresentableRepresentation) jsonConverter.deserialize(s)).recreateRepresentable())
                .collect(Collectors.toList());
    }

    /**
     * Wrapper around DELETE requests for deleting all promotions in the system.
     *
     * @param webTestClient  test client used to make request to the server
     * @param providerSecret shared secret used to authenticate the request
     * @param expectedStatus if another HTTP status than this is sent back for the query, an exception is thrown
     */
    static void deleteAllPromotions(WebTestClient webTestClient, String providerSecret, HttpStatus expectedStatus) {
        webTestClient
                .delete()
                .uri("/promotions")
                .header("provider-secret", providerSecret)
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    /**
     * Wrapper around the Issue endpoint of the incentive service
     * which the client contacts to execute the Issue-Join protocol with the server for the passed promotion.
     * Lets the server execute Issue-Join with the passed WebTestClient.
     *
     * @param webTestClient   test client used to make request to the server
     * @param incentiveSystem incentive system instance for which the protocol is run
     * @param pkp             provider SPS-EQ public key that is used to authenticate the token
     * @param ukp             key pair of the user making the request (user public key, user secret key)
     * @param promotion       promotion object
     * @param expectedStatus  if another HTTP status than this is sent back for the query, an exception is thrown
     * @return token object, representing empty token that user obtains after successful Issue-Join interaction
     */
    static Token joinPromotion(WebTestClient webTestClient,
                               IncentiveSystem incentiveSystem,
                               ProviderKeyPair pkp,
                               UserKeyPair ukp,
                               Promotion promotion,
                               HttpStatus expectedStatus) {
        // fix the pseudorandomness for this protocol execution + generate join request
        var joinFirstStepOutput = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp);

        // send join request to server (server executes Issue to generate the obtained join response)
        var serializedJoinResponse = webTestClient.post()
                .uri("/join-promotion")
                .header("user-public-key", jsonConverter.serialize(ukp.getPk().getRepresentation()))
                .header("join-request", jsonConverter.serialize(joinFirstStepOutput.getJoinRequest().getRepresentation()))
                .header("promotion-id", String.valueOf(promotion.getPromotionParameters().getPromotionId()))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        // de-marshall join response
        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), incentiveSystem.pp);

        // user computes and returns token by executing second part of Join algorithm
        return incentiveSystem.handleJoinRequestResponse(promotion.getPromotionParameters(), pkp.getPk(), joinFirstStepOutput, joinResponse);
    }


    public static Token earn(WebTestClient webClient, IncentiveSystem incentiveSystem, ProviderKeyPair pkp, UserKeyPair ukp, Token token, Vector<BigInteger> pointsToEarn, EarnStoreResponse earnStoreResponse, PromotionParameters promotionParameters) {
        var earnRequest = incentiveSystem.generateEarnRequest(token, pkp.getPk(), ukp, pointsToEarn, earnStoreResponse);
        var earnDto = new EarnRequestProviderDto(promotionParameters.getPromotionId(), jsonConverter.serialize(earnRequest.getRepresentation()));
        BulkRequestProviderDto bulkRequestProviderDto = new BulkRequestProviderDto(Collections.emptyList(), List.of(earnDto));
        BulkResultsProviderDto bulkResultsProviderDto = bulkWithProvider(webClient, bulkRequestProviderDto);
        var serializedEarnResponse = bulkResultsProviderDto.getEarnResults().get(0).getSerializedEarnResponse();
        var earnResponse = new SPSEQSignature(jsonConverter.deserialize(serializedEarnResponse), incentiveSystem.pp.getBg().getG1(), incentiveSystem.pp.getBg().getG2());
        return incentiveSystem.handleEarnResponse(earnRequest, earnResponse, promotionParameters, token, ukp, pkp.getPk());
    }

    public static SpendProviderResponse spend(WebTestClient webClient, IncentiveSystem incentiveSystem, PromotionParameters promotionParameters, ZkpTokenUpdate tokenUpdate, SpendProviderRequest spendProviderRequest, ZkpTokenUpdateMetadata metadata, UUID basketId, List<BigInteger> basketPoints) {
        var spendRequestDto = new SpendRequestProviderDto(promotionParameters.getPromotionId(),
                jsonConverter.serialize(spendProviderRequest.getRepresentation()),
                jsonConverter.serialize(new RepresentableRepresentation(metadata)),
                basketId,
                tokenUpdate.getTokenUpdateId(),
                basketPoints);
        BulkRequestProviderDto bulkRequestProviderDto = new BulkRequestProviderDto(List.of(spendRequestDto), Collections.emptyList());
        BulkResultsProviderDto bulkResultsProviderDto = bulkWithProvider(webClient, bulkRequestProviderDto);
        var serializedSpendResponse = bulkResultsProviderDto.getSpendResults().get(0).getSerializedSpendResult();
        return new SpendProviderResponse(jsonConverter.deserialize(serializedSpendResponse), incentiveSystem.pp);
    }

    public static BulkResultsProviderDto bulkWithProvider(WebTestClient webClient,
                                                          BulkRequestProviderDto bulkRequestProviderDto) {
        return webClient.post()
                .uri("/bulk")
                .body(BodyInserters.fromValue(bulkRequestProviderDto))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(BulkResultsProviderDto.class)
                .returnResult()
                .getResponseBody();
    }


    /*
     * helper methods
     */

    /**
     * Computes a serialized representation of a promotion object.
     * Used when marshalling a promotion for adding it to the system.
     */
    private static List<String> serializePromotionsWithType(Promotion promotionToAdd) {
        return List.of(jsonConverter.serialize(new RepresentableRepresentation(promotionToAdd)));
    }
}
