package org.cryptimeleon.incentive.services.basket;

import io.swagger.annotations.ApiOperation;
import org.cryptimeleon.incentive.client.dto.store.BulkRequestStoreDto;
import org.cryptimeleon.incentive.client.dto.store.BulkResultsStoreDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
public class StoreController {
    // Will be set via dependency injection
    private final StoreService storeService;
    @Value("${basket-service.provider-secret}")
    private String basketServiceProviderSecret; // used to authenticate the request for the store secret key (set via environment variable)

    @Value("${store-name}")
    private String storeName;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * Get the name of this store.
     */
    @GetMapping("/name")
    ResponseEntity<String> name() {
        return new ResponseEntity<>(storeName, HttpStatus.OK);
    }

    /**
     * This would happen after somebody at a store verifies the userInfo, e.g. an id.
     *
     * @param serializedUserPublicKey the public key of the user that wants to register
     * @param userInfo                some information that allows identifying a user in the real world
     * @return a serialized registration coupon consisting of the signed data and the verification key
     */
    @GetMapping("/register")
    ResponseEntity<String> registerUserAndReturnSerializedRegistrationCoupon(@RequestHeader("user-public-key") String serializedUserPublicKey, @RequestHeader("user-info") String userInfo) {
        return new ResponseEntity<>(storeService.registerUserAndReturnSerializedRegistrationCoupon(serializedUserPublicKey, userInfo), HttpStatus.OK);
    }

    /**
     * Send a bulk request, i.e., a bundle of earn and spend requests associated to the same basket.
     * The requests are processed, but the results held back until the corresponding basket is paid.
     *
     * @param bulkRequestStoreDto a dto containing all individual token update requests
     */
    @PostMapping("/bulk")
    ResponseEntity<Void> bulkRequest(@RequestBody BulkRequestStoreDto bulkRequestStoreDto) {
        storeService.processBulkRequest(bulkRequestStoreDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Obtain the bulk results for some basket if present and the basket is paid.
     *
     * @param basketId the id of the basket
     * @return a DTO containing serialized results
     */
    @GetMapping("/bulk-results")
    ResponseEntity<BulkResultsStoreDto> bulkResponse(@RequestHeader("basket-id") UUID basketId) {
        return new ResponseEntity<>(storeService.bulkResponses(basketId), HttpStatus.OK);
    }

    /**
     * HTTP endpoint for obtaining list of all promotions in the system.
     *
     * @return response entity that holds list of strings
     */
    @GetMapping("/promotions")
    @ApiOperation(value = "Query all Promotion", response = String.class)
    public ResponseEntity<String[]> getPromotions() {
        return new ResponseEntity<>(storeService.getPromotions(), HttpStatus.OK);
    }

    /**
     * HTTP endpoint for adding new promotions (sent via a list of strings (serialized representations) in request body) to the system.
     * Authorized action, requires passing the provider secret via a header in the HTTP request.
     *
     * @param storeSecretHeader    password sent via a header (is compared to provider secret)
     * @param serializedPromotions list of strings
     * @return void response entity
     */
    @PostMapping("/promotions")
    @ApiOperation("Add new Promotions")
    public ResponseEntity<Void> addPromotions(@RequestHeader("store-secret") String storeSecretHeader, @RequestBody List<String> serializedPromotions) {
        if (storeSecretHeader == null || !storeSecretHeader.equals(basketServiceProviderSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        storeService.addPromotions(serializedPromotions);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * HTTP endpoint for deleting all promotions from the system.
     * Authorized action, requires passing the provider secret via a header in the HTTP request.
     *
     * @param storeSecretHeader password sent via a header (is compared to provider secret)
     * @return void response entity
     */
    @DeleteMapping("/promotions")
    @ApiOperation("Delete all existing Promotions")
    public ResponseEntity<Void> deleteAllPromotions(@RequestHeader("store-secret") String storeSecretHeader) {
        if (storeSecretHeader == null || !storeSecretHeader.equals(basketServiceProviderSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        storeService.deleteAllPromotions();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler(StoreException.class)
    public ResponseEntity<String> handleException(StoreException ex) {
        // For debugging causes send the exception string
        return new ResponseEntity<>("An exception occurred!\n" + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
