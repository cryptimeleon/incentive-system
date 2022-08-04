package org.cryptimeleon.incentive.services.promotion;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.EarnRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResultsDto;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.EarnRequest;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.JoinResponse;
import org.cryptimeleon.incentive.promotion.Promotion;
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

public class ClientHelper {

    private static final JSONConverter jsonConverter = new JSONConverter();

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

    private static List<String> serializePromotionsWithType(Promotion promotionToAdd) {
        return List.of(jsonConverter.serialize(new RepresentableRepresentation(promotionToAdd)));
    }

    static List<Promotion> getPromotions(@Autowired WebTestClient webClient) {
        String[] newSerializedPromotions = webClient
                .get()
                .uri("/promotions")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String[].class)
                .returnResult()
                .getResponseBody();

        assert newSerializedPromotions != null;
        return Arrays.stream(newSerializedPromotions)
                .map(s -> (Promotion) ((RepresentableRepresentation) jsonConverter.deserialize(s)).recreateRepresentable())
                .collect(Collectors.toList());
    }

    static void deleteAllPromotions(WebTestClient webTestClient, String providerSecret, HttpStatus expectedStatus) {
        webTestClient
                .delete()
                .uri("/promotions")
                .header("provider-secret", providerSecret)
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus);
    }

    static Token joinPromotion(WebTestClient webTestClient,
                               IncentiveSystem incentiveSystem,
                               ProviderKeyPair pkp,
                               UserKeyPair ukp,
                               Promotion promotion,
                               HttpStatus expectedStatus) {
        var joinRequest = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp, promotion.getPromotionParameters());

        // Send request and process response to assert correct behavior
        var serializedJoinResponse = webTestClient.post()
                .uri("/join-promotion")
                .header("user-public-key", jsonConverter.serialize(ukp.getPk().getRepresentation()))
                .header("join-request", jsonConverter.serialize(joinRequest.getRepresentation()))
                .header("promotion-id", String.valueOf(promotion.getPromotionParameters().getPromotionId()))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        var joinResponse = new JoinResponse(jsonConverter.deserialize(serializedJoinResponse), incentiveSystem.pp);
        return incentiveSystem.handleJoinRequestResponse(promotion.getPromotionParameters(), pkp.getPk(), ukp, joinRequest, joinResponse);
    }

    static EarnRequest generateAndSendEarnRequest(WebTestClient webTestClient,
                                                  IncentiveSystem incentiveSystem,
                                                  ProviderKeyPair pkp,
                                                  UserKeyPair ukp,
                                                  Token token,
                                                  BigInteger promotionId,
                                                  UUID basketId,
                                                  HttpStatus expectedStatus) {
        var earnRequest = incentiveSystem.generateEarnRequest(token, pkp.getPk(), ukp);
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
        return earnRequest;
    }

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

        var resultsDto = webTestClient.post()
                .uri("/bulk-token-update-results")
                .header("basket-id", String.valueOf(basketId))
                .exchange()
                .expectStatus()
                .isEqualTo(expectedStatus)
                .expectBody(TokenUpdateResultsDto.class)
                .returnResult().getResponseBody();
        assert resultsDto != null;
        var serializedEarnResponse = resultsDto.getEarnTokenUpdateResultDtoList().get(0).getSerializedEarnResponse();
        SPSEQSignature spseqSignature = new SPSEQSignature(jsonConverter.deserialize(serializedEarnResponse), pp.getBg().getG1(), pp.getBg().getG2());

        return incentiveSystem.handleEarnRequestResponse(promotion.getPromotionParameters(), earnRequest, spseqSignature, pointsToEarn, token, pkp.getPk(), ukp);
    }
}
