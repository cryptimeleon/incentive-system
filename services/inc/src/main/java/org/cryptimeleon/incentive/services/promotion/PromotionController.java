package org.cryptimeleon.incentive.services.promotion;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.cryptimeleon.incentive.client.dto.inc.BulkRequestDto;
import org.cryptimeleon.incentive.client.dto.inc.TokenUpdateResultsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * The controller of this service that defines all REST endpoints.
 */
@RestController
@AllArgsConstructor
public class PromotionController {

    private PromotionService promotionService;

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
                promotionService.getPromotions(),
                HttpStatus.OK
        );
    }

    @PostMapping("/promotions")
    @ApiOperation(value = "Add new Promotions")
    public void addPromotions(@RequestBody List<String> serializedPromotions) {
        // TODO authenticated endpoint
        promotionService.addPromotions(serializedPromotions);
    }


    @PostMapping("/join-promotion")
    public ResponseEntity<String> joinPromotion(
            @RequestHeader(name = "promotion-id") BigInteger promotionId,
            @RequestHeader(name = "join-request") String serializedJoinRequest,
            @RequestHeader(name = "user-public-key") String serializedUserPublicKey
    ) {
        return new ResponseEntity<>(promotionService.joinPromotion(promotionId, serializedJoinRequest, serializedUserPublicKey), HttpStatus.OK);
    }

    @PostMapping("/bulk-token-updates")
    public void bulkUpdates(
            @RequestHeader(name = "basket-id") UUID basketId,
            @RequestBody BulkRequestDto bulkRequestDto
    ) {
        promotionService.handleBulk(basketId, bulkRequestDto);
    }

    @PostMapping("/bulk-token-update-results")
    public ResponseEntity<TokenUpdateResultsDto> bulkResults(
            @RequestHeader(name = "basket-id") UUID basketId
    ) {
        return new ResponseEntity<>(promotionService.retrieveBulkResults(basketId), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IncentiveServiceException.class)
    public String handleIncentiveException(RuntimeException ex) {
        return "An exception occurred!\n" + ex.getMessage();
    }
}