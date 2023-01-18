package org.cryptimeleon.incentive.services.incentive;

import io.swagger.annotations.ApiOperation;
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
 * The controller of the incentive service that defines all REST endpoints.
 * Maps GET and POST requests to the respective actions to be executed.
 * </br>
 * Provides endpoints to
 * - manage promotions in the system
 * - interact with a provider of the incentive system to join promotions, earn points and claim rewards
 * - take the double-spending database down for a short time
 * (for demonstration purposes only; the outgoing queue of spend transactions will be stopped to be fed into the double-spending database)
 */
@RestController
public class IncentiveController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IncentiveController.class);
    // ref to service that handles server side of crypto protocols + genesis token issuing
    private final IncentiveService incentiveService;
    // ref to service that handles temporary DoS of double-spending protection service
    private final DosService dosService;
    // shared secret, required to perform privileged actions
    @Value("${incentive-service.provider-secret}")
    private String providerSecret;

    /**
     * Checks if shared secret for privileged actions is set properly.
     * Throws an exception if not.
     */
    @PostConstruct
    public void validateValue() {
        if (providerSecret.equals("")) {
            throw new IllegalArgumentException("Basket provider secret is not set!");
        }
        log.info("Provider secret: {}", providerSecret);
    }

    /**
     * HTTP endpoint for alive testing etc.
     */
    @GetMapping("/")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello from incentive service!", HttpStatus.OK);
    }

    /*
     * endpoints for managing promotions in the system
     */
    public IncentiveController(final IncentiveService incentiveService, final DosService dosService) {
        this.incentiveService = incentiveService;
        this.dosService = dosService;
    }

    /**
     * HTTP endpoint for obtaining list of all promotions in the system.
     * @return response entity that holds list of strings
     */
    @GetMapping("/promotions")
    @ApiOperation(value = "Query all Promotion", response = String.class)
    public ResponseEntity<String[]> getPromotions() {
        return new ResponseEntity<>(incentiveService.getPromotions(), HttpStatus.OK);
    }

    /**
     * HTTP endpoint for adding new promotions (sent via a list of strings (serialized representations) in request body) to the system.
     * Authorized action, requires passing the provider secret via a header in the HTTP request.
     * @param providerSecretHeader password sent via a header (is compared to provider secret)
     * @param serializedPromotions list of strings
     * @return void response entity
     */
    @PostMapping("/promotions")
    @ApiOperation("Add new Promotions")
    public ResponseEntity<Void> addPromotions(@RequestHeader("provider-secret") String providerSecretHeader, @RequestBody List<String> serializedPromotions) {
        if (providerSecretHeader == null || !providerSecretHeader.equals(providerSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        incentiveService.addPromotions(serializedPromotions);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /*
     * end of endpoints for managing promotions in the system
     */
    /*
     * endpoints for the user to interact with a provider of the incentive system
     */

    /**
     * HTTP endpoint for deleting all promotions from the system.
     * Authorized action, requires passing the provider secret via a header in the HTTP request.
     *
     * @param providerSecretHeader password sent via a header (is compared to provider secret)
     * @return void response entity
     */
    @DeleteMapping("/promotions")
    @ApiOperation("Delete all existing Promotions")
    public ResponseEntity<Void> deleteAllPromotions(@RequestHeader("provider-secret") String providerSecretHeader) {
        if (providerSecretHeader == null || !providerSecretHeader.equals(providerSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        incentiveService.deleteAllPromotions();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * HTTP endpoint for joining the system by obtaining a genesis token.
     *
     * @param serializedUserPublicKey serialized representation of the public key of the user joining the system
     * @return serialized representation of a genesis signature for this user
     */
    @PostMapping("/genesis")
    @Deprecated
    public ResponseEntity<String> joinSystem(@RequestHeader(name = "user-public-key") String serializedUserPublicKey) {
        return new ResponseEntity<>(incentiveService.generateGenesisSignature(serializedUserPublicKey), HttpStatus.OK);
    }

    /**
     * HTTP endpoint for joining the system by obtaining a registration token by showing a valid registration coupon
     * signed by a trusted store.
     *
     * @param serializedRegistrationCoupon the registration coupon
     * @return serialized representation of a registration signature for this user
     */
    @PostMapping("/register-with-coupon")
    @Deprecated
    public ResponseEntity<String> registerAsUser(@RequestHeader(name = "registration-coupon") String serializedRegistrationCoupon) {
        return new ResponseEntity<>(incentiveService.registerUser(serializedRegistrationCoupon), HttpStatus.OK);
    }

    /**
     * HTTP endpoint for joining the promotion that is identified by the passed promotion ID.
     * Joining a promotion means obtaining an empty token (i.e. with 0 points) for this promotion by executing the Issue-Join protocol with the server.
     * This is done by sending a join request to this endpoint.
     *
     * @param promotionId           ID of the promotion that the user wants to join
     * @param serializedJoinRequest serialized representation of a join request
     * @return response entity object containing a serialized representation of a join response
     */
    @PostMapping("/join-promotion")
    public ResponseEntity<String> joinPromotion(@RequestHeader(name = "promotion-id") BigInteger promotionId, @RequestHeader(name = "join-request") String serializedJoinRequest) {
        return new ResponseEntity<>(incentiveService.joinPromotion(promotionId, serializedJoinRequest), HttpStatus.OK);
    }

    /**
     * HTTP endpoint for sending a bulk of spend and earn requests to the incentive server.
     * Server will apply the updates to the basket identified by the passed basket ID
     * and store the results (i.e. granted rewards and earned points) for later
     * (since points and rewards can only be obtained after basket is paid).
     *
     * @param basketId       ID of the basket to apply the updates to
     * @param bulkRequestDto data transfer object (DTO) containing spend and earn requests
     */
    @PostMapping("/bulk-token-updates")
    public void bulkUpdates(@RequestHeader(name = "basket-id") UUID basketId, @RequestBody BulkRequestDto bulkRequestDto) {
        incentiveService.handleBulk(basketId, bulkRequestDto);
    }

    /*
    * end of endpoints for user to interact with provider of incentive system
    */
    /*
    * endpoints for simulating denial-of-service attacks on the double-spending database.
    */
    /**
     * HTTP endpoint to start a short simulated DoS attack on the double-spending protection database.
     */
    @GetMapping("/dos/short-duration")
    public void shortDos() {
        dosService.addShortWaitPeriod();
    }

    /**
     * HTTP endpoint to start a longer simulated DoS attack on the double-spending protection database.
     */
    @GetMapping("/dos/long-duration")
    public void longDos() {
        dosService.addLongWaitPeriod();
    }

    /**
     * HTTP endpoint to "withdraw" demo denial-of-service attacks on the double-spending protection service
     * and make it available again instantly.
     */
    @GetMapping("/dos/stop")
    public void stopDos() {
        dosService.removeAllWaitPeriod();
    }

    /**
     * HTTP endpoint to query the remaining time that the double-spending protection service will be down due to demo denial-of-service attacks.
     * @return time in seconds
     */
    @GetMapping("/dos/remaining-offline-time")
    public long remainingOfflineTimeSeconds() {
        return dosService.getRemainingOfflineTimeSeconds();
    }

    /*
    * end of endpoints for simulating denial-of-service attacks on the double-spending database
    */
    /*
    * Exception handling
    */
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

    /**
     * HTTP endpoint for obtaining all points and rewards for a paid basket identified by the passed ID.
     *
     * @return data transfer object (DTO) with all the updates
     */
    @PostMapping("/bulk-token-update-results")
    public TokenUpdateResultsDto bulkResults(@RequestHeader(name = "basket-id") UUID basketId) {
        return incentiveService.retrieveBulkResults(basketId);
    }
}
