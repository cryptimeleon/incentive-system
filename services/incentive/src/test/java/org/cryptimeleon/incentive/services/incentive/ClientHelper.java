package org.cryptimeleon.incentive.services.incentive;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.EarnRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResultsDto;
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
     * @param webClient test client used to make request to server
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
     * @param webTestClient test client used to make request to the server
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
     * @param webTestClient test client used to make request to the server
     * @param incentiveSystem incentive system instance for which the protocol is run
     * @param pkp provider SPS-EQ public key that is used to authenticate the token
     * @param ukp key pair of the user making the request (user public key, user secret key)
     * @param promotion promotion object
     * @param expectedStatus if another HTTP status than this is sent back for the query, an exception is thrown
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

    /**
     * Helper method that generates an earn request and sends it to the Credit endpoint of the incentive server.
     *
     * @param webTestClient test client to send requests to server
     * @param incentiveSystem incentive system instance used in this protocol run
     * @param pkp SPS-EQ public key of the provider of the incentive system
     * @param ukp key pair of the user (user public key, user secret key)
     * @param token user token to update
     * @param promotionId ID of the promotion that the user wants to earn points for
     * @param basketId ID of the basket of the user (which is used to compute the earned points)
     * @param expectedStatus if any other status than this is returned upon the client's request, an exception is throwns
     * @return the request that can processed by the provider
     */
    static EarnRequest generateAndSendEarnRequest(WebTestClient webTestClient,
                                                  IncentiveSystem incentiveSystem,
                                                  ProviderKeyPair pkp,
                                                  UserKeyPair ukp,
                                                  Token token,
                                                  BigInteger promotionId,
                                                  UUID basketId,
                                                  HttpStatus expectedStatus) {
        // compute earn request from token and keys
        var earnRequest = incentiveSystem.generateEarnRequest(token, pkp.getPk(), ukp);

        // make earn request
        webTestClient.post()
                .uri("/bulk-token-updates")
                .header("basket-id", String.valueOf(basketId))
                .body(BodyInserters.fromValue(
                        new BulkRequestDto(
                                List.of(new EarnRequestDto(promotionId,
                                        jsonConverter.serialize(earnRequest.getRepresentation()))),
                                Collections.emptyList())))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);

        // return earn request (is needed later to retrieve the token after the provider executed the Credit algorithm)
        return earnRequest;
    }

    /**
     * Helper method.
     * User claims the points she earned with the passed earn request.
     * The separation from making the earn request is necessary
     * since the user's basket needs to be paid after making the earn request and before claiming the points.
     *
     * @param webTestClient test client (= user) making the requests to the server
     * @param incentiveSystem incentive system instance used in this protocol run
     * @param promotion promotion object, modelling the promotion for which the user is claiming points
     * @param pkp SPS-EQ public key of the provider of the incentive system
     * @param ukp key pair of the user
     * @param token user token to update
     * @param earnRequest initial corresponding earn request that was made before paying the basket
     * @param basketId ID of the basket that the user wants to claim points for
     * @param pointsToEarn how many points the user earns for her basket
     * @param expectedStatus if any other HTTP status than this one is returned, an exception is thrown
     * @return updated token
     */
    public static Token retrieveTokenAfterEarn(WebTestClient webTestClient,
                                               IncentiveSystem incentiveSystem,
                                               Promotion promotion,
                                               ProviderKeyPair pkp,
                                               UserKeyPair ukp,
                                               Token token,
                                               EarnRequest earnRequest,
                                               UUID basketId,
                                               Vector<BigInteger> pointsToEarn,
                                               HttpStatus expectedStatus) {
        var pp = incentiveSystem.pp;

        // make request
        var resultsDto = webTestClient.post()
                .uri("/bulk-token-update-results")
                .header("basket-id", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectBody(TokenUpdateResultsDto.class)
                .returnResult().getResponseBody();

        // assert well-formedness of result
        assert resultsDto != null;

        // extract earn response
        var serializedEarnResponse = resultsDto.getEarnTokenUpdateResultDtoList().get(0).getSerializedEarnResponse();

        // extract updated signature from earn responses
        SPSEQSignature spseqSignature = new SPSEQSignature(jsonConverter.deserialize(serializedEarnResponse), pp.getBg().getG1(), pp.getBg().getG2());

        // execute second part of Earn algorithm and output resulting token
        return incentiveSystem.handleEarnRequestResponse(promotion.getPromotionParameters(), earnRequest, spseqSignature, pointsToEarn, token, pkp.getPk(), ukp);
    }


    public static Token earn(WebTestClient webClient, IncentiveSystem incentiveSystem, ProviderKeyPair pkp, UserKeyPair ukp, Token token, Vector<BigInteger> pointsToEarn, EarnStoreCouponSignature earnStoreCouponSignature, PromotionParameters promotionParameters) {
        var earnRequest = incentiveSystem.generateEarnRequest(token, pkp.getPk(), ukp, pointsToEarn, earnStoreCouponSignature);
        var earnDto = new EarnRequestProviderDto(promotionParameters.getPromotionId(), jsonConverter.serialize(earnRequest.getRepresentation()));
        BulkRequestProviderDto bulkRequestProviderDto = new BulkRequestProviderDto(Collections.emptyList(), List.of(earnDto));
        BulkResultsProviderDto bulkResultsProviderDto = bulkWithProvider(webClient, bulkRequestProviderDto);
        var serializedEarnResponse = bulkResultsProviderDto.getEarnResults().get(0).getSerializedEarnResponse();
        var earnResponse = new SPSEQSignature(jsonConverter.deserialize(serializedEarnResponse), incentiveSystem.pp.getBg().getG1(), incentiveSystem.pp.getBg().getG2());
        return incentiveSystem.handleEarnResponse(earnRequest, earnResponse, promotionParameters, token, ukp, pkp.getPk());
    }

    public static SpendResponseECDSA spend(WebTestClient webClient, IncentiveSystem incentiveSystem, PromotionParameters promotionParameters, ZkpTokenUpdate tokenUpdate, SpendRequestECDSA spendRequestECDSA, ZkpTokenUpdateMetadata metadata, UUID basketId, List<BigInteger> basketPoints) {
        var spendRequestDto = new SpendRequestProviderDto(promotionParameters.getPromotionId(),
                jsonConverter.serialize(spendRequestECDSA.getRepresentation()),
                jsonConverter.serialize(new RepresentableRepresentation(metadata)),
                basketId,
                tokenUpdate.getTokenUpdateId(),
                basketPoints);
        BulkRequestProviderDto bulkRequestProviderDto = new BulkRequestProviderDto(List.of(spendRequestDto), Collections.emptyList());
        BulkResultsProviderDto bulkResultsProviderDto = bulkWithProvider(webClient, bulkRequestProviderDto);
        var serializedSpendResponse = bulkResultsProviderDto.getSpendResults().get(0).getSerializedSpendResult();
        var spendResponse = new SpendResponseECDSA(jsonConverter.deserialize(serializedSpendResponse), incentiveSystem.pp);
        return spendResponse;
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
