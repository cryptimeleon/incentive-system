package org.cryptimeleon.incentive.services.promotion;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class PromotionController {

    private PromotionService promotionService;

    /**
     * Endpoint for alive testing etc.
     */
    @GetMapping("/")
    public ResponseEntity<String> test() {
        return new ResponseEntity<>("Hello from Promotion service!", HttpStatus.OK);
    }

    @GetMapping("/promotions")
    @ApiOperation(value = "Query all Promotion", response = String.class)
    public ResponseEntity<String[]> getPromotions() {
        return new ResponseEntity<>(
                promotionService.getPromotions(),
                HttpStatus.OK
        );
    }

    @PostMapping("/join-promotion")
    public ResponseEntity<String> joinPromotion(
            @RequestHeader(name = "promotion-id") BigInteger promotionId,
            @RequestHeader(name = "join-request") String serializedJoinRequest,
            @RequestHeader(name = "user-public-key") String serializedUserPublicKey
    ) {
        return new ResponseEntity<>(promotionService.joinPromotion(promotionId, serializedJoinRequest, serializedUserPublicKey), HttpStatus.OK);
    }


    @PostMapping("/earn")
    public ResponseEntity<String> earnPoints(
            @RequestHeader(name = "promotion-id") BigInteger promotionId,
            @RequestHeader(name = "earn-request") String serializedEarnRequest,
            @RequestHeader(name = "basket-id") UUID basketId
    ) {
        return new ResponseEntity<>(promotionService.handleEarnRequest(promotionId, serializedEarnRequest, basketId), HttpStatus.OK);
    }

    @PostMapping("/spend")
    public ResponseEntity<String> spendPoints(
            @RequestHeader(name = "promotion-id") BigInteger promotionId,
            @RequestHeader(name = "spend-request") String serializedSpendRequest,
            @RequestHeader(name = "basket-id") UUID basketId,
            @RequestHeader(name = "reward-id") UUID rewardId
    ) {
        return new ResponseEntity<>(promotionService.handleSpendRequest(promotionId, basketId, rewardId, serializedSpendRequest), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(RuntimeException.class)
    public String handleIncentiveException(RuntimeException ex) {
        return "An exception occurred!";
    }
}