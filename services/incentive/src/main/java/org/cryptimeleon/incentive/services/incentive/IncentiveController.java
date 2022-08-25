package org.cryptimeleon.incentive.services.incentive;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResultsDto;
import org.cryptimeleon.incentive.services.incentive.error.BasketAlreadyPaidException;
import org.cryptimeleon.incentive.services.incentive.error.BasketNotPaidException;
import org.cryptimeleon.incentive.services.incentive.error.IncentiveServiceException;
import org.cryptimeleon.incentive.services.incentive.error.OnlineDoubleSpendingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * The controller of this service that defines all REST endpoints.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class IncentiveController {

    private final IncentiveService incentiveService;
    private final DosService dosService;

    @Value("${incentive-service.provider-secret}")
    private String providerSecret;

    @PostConstruct
    public void validateValue() {
        if (providerSecret.equals("")) {
            throw new IllegalArgumentException("Basket provider secret is not set!");
        }

        log.info("Provider secret: {}", providerSecret);
    }

    /**
     * Endpoint for alive testing etc.
     */
    @GetMapping("/")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello from incentive service!", HttpStatus.OK);
    }

    @GetMapping("/promotions")
    @ApiOperation(value = "Query all Promotion", response = String.class)
    public ResponseEntity<String[]> getPromotions() {
        return new ResponseEntity<>(
                incentiveService.getPromotions(),
                HttpStatus.OK
        );
    }

    @PostMapping("/promotions")
    @ApiOperation(value = "Add new Promotions")
    public ResponseEntity<Void> addPromotions(@RequestHeader("provider-secret") String providerSecretHeader, @RequestBody List<String> serializedPromotions) {
        if (providerSecretHeader == null || !providerSecretHeader.equals(providerSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        incentiveService.addPromotions(serializedPromotions);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/promotions")
    @ApiOperation(value = "Add new Promotions")
    public ResponseEntity<Void> deleteAllPromotions(@RequestHeader("provider-secret") String providerSecretHeader) {
        if (providerSecretHeader == null || !providerSecretHeader.equals(providerSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        incentiveService.deleteAllPromotions();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/genesis")
    public ResponseEntity<String> joinPromotion(
            @RequestHeader(name = "user-public-key") String serializedUserPublicKey
    ) {
        return new ResponseEntity<>(incentiveService.generateGenesisSignature(serializedUserPublicKey), HttpStatus.OK);
    }

    @PostMapping("/join-promotion")
    public ResponseEntity<String> joinPromotion(
            @RequestHeader(name = "promotion-id") BigInteger promotionId,
            @RequestHeader(name = "join-request") String serializedJoinRequest
    ) {
        return new ResponseEntity<>(incentiveService.joinPromotion(promotionId, serializedJoinRequest), HttpStatus.OK);
    }

    @PostMapping("/bulk-token-updates")
    public void bulkUpdates(
            @RequestHeader(name = "basket-id") UUID basketId,
            @RequestHeader(name= "do-sync") String doSyncString,
            @RequestBody BulkRequestDto bulkRequestDto
    ) {
        boolean doSync = doSyncString.equals("true");
        incentiveService.handleBulk(basketId, bulkRequestDto, doSync);
    }

    @PostMapping("/bulk-token-update-results")
    public TokenUpdateResultsDto bulkResults(
            @RequestHeader(name = "basket-id") UUID basketId
    ) {
        return incentiveService.retrieveBulkResults(basketId);
    }


    @GetMapping("/dos/short-duration")
    public void shortDos() {
        dosService.addShortWaitPeriod();
    }

    @GetMapping("/dos/long-duration")
    public void longDos() {
        dosService.addLongWaitPeriod();
    }

    @GetMapping("/dos/stop")
    public void stopDos() {
        dosService.removeAllWaitPeriod();
    }

    @GetMapping("/dos/remaining-offline-time")
    public long remainingOfflineTimeSeconds() {
        return dosService.getRemainingOfflineTimeSeconds();
    }

    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    @ExceptionHandler(OnlineDoubleSpendingException.class)
    public String handleOnlineDSPException() {
        return "Double-spending attempt detected and prevented!";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BasketAlreadyPaidException.class)
    public String handleBasketAlreadyPaidException() {
        return "Basket already payed and thus cannot be used for promotions!";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BasketNotPaidException.class)
    public String handleBasketNotPaidException() {
        return "Cannot retrieve token update results! Basket must be payed!";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IncentiveServiceException.class)
    public String handleException(IncentiveServiceException ex) {
        // For debugging causes send the exception string
        return "An exception occurred!\n" + ex.getMessage();
    }
}
