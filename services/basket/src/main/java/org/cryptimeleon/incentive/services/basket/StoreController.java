package org.cryptimeleon.incentive.services.basket;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class StoreController {
    private final StoreService storeService;
    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * This would happen after somebody at a store verifies the userInfo, e.g. an id.
     *
     * @param serializedUserPublicKey the public key of the user that wants to register
     * @param userInfo some information that allows identifying a user in the real world
     * @return a serialized registration coupon consisting of the signed data and the verification key
     */
    @GetMapping("/register-user-and-obtain-serialized-registration-coupon")
    ResponseEntity<String> registerUserAndReturnSerializedRegistrationCoupon(@RequestHeader("user-public-key") String serializedUserPublicKey, @RequestHeader("user-info") String userInfo) {
        return new ResponseEntity<>(storeService.registerUserAndReturnSerializedRegistrationCoupon(serializedUserPublicKey, userInfo), HttpStatus.OK);
    }
}
