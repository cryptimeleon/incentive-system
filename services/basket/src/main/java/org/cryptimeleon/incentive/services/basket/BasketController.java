package org.cryptimeleon.incentive.services.basket;

import lombok.extern.slf4j.Slf4j;
import org.cryptimeleon.incentive.services.basket.exceptions.*;
import org.cryptimeleon.incentive.services.basket.model.Basket;
import org.cryptimeleon.incentive.services.basket.model.Item;
import org.cryptimeleon.incentive.services.basket.model.RewardItem;
import org.cryptimeleon.incentive.services.basket.model.requests.PutItemRequest;
import org.cryptimeleon.incentive.services.basket.model.requests.RedeemBasketRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;


/**
 * A REST controller that defines and handles all requests of the basket server.
 */
@Slf4j
@RestController
public class BasketController {

    private BasketService basketService;  // Spring boot automatically injects a BasketService object

    @Value("${basket-service.pay-secret}")
    private String paymentSecret;

    @Value("${basket-service.redeem-secret}")
    private String redeemSecret;

    @Value("${basket-service.provider-secret}")
    private String providerSecret;


    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    /**
     * Make sure that the shared secrets are set.
     */
    @PostConstruct
    public void validateValue() {
        if (paymentSecret.equals("")) {
            throw new IllegalArgumentException("Payment secret is not set!");
        }
        if (redeemSecret.equals("")) {
            throw new IllegalArgumentException("Redeem secret is not set!");
        }
        if (providerSecret.equals("")) {
            throw new IllegalArgumentException("Basket provider secret is not set!");
        }

        log.info("Payment secret: {}", paymentSecret);
        log.info("Redeem secret: {}", redeemSecret);
        log.info("Provider secret: {}", providerSecret);
    }

    /**
     * Can be used for health checking.
     */
    @GetMapping("/")
    String getHelloWorld() {
        return "Hello from Basket service!";
    }


    /**
     * Query all shopping items that can be purchased
     */
    @GetMapping("/items")
    Item[] getAllBasketItems() {
        return basketService.getItems();
    }

    @PostMapping("/items")
    ResponseEntity<Void> newItem(@RequestHeader("provider-secret") String providerSecretHeader, @RequestBody Item item) {
        if (providerSecretHeader == null || !providerSecretHeader.equals(providerSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        basketService.save(item);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/items")
    ResponseEntity<Void> deleteAllItems(@RequestHeader("provider-secret") String providerSecretHeader) {
        if (providerSecretHeader == null || !providerSecretHeader.equals(providerSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        basketService.deleteAllItems();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Query shopping item by id, e.g. EAN13
     */
    @GetMapping("/items/{id}")
    ResponseEntity<Item> getBasketItemById(@PathVariable String id) {
        var item = basketService.getItem(id);
        if (item != null) {
            return new ResponseEntity<>(item, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Query all shopping items that can be purchased
     */
    @GetMapping("/reward-items")
    RewardItem[] getAllRewardItems() {
        return basketService.getRewardItems();
    }

    @PostMapping("/reward-items")
    ResponseEntity<Void> newRewardItem(@RequestHeader("provider-secret") String providerSecretHeader, @RequestBody RewardItem rewardItem) {
        if (providerSecretHeader == null || !providerSecretHeader.equals(providerSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        basketService.save(rewardItem);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/reward-items")
    ResponseEntity<Void> deleteAllRewardItems(@RequestHeader("provider-secret") String providerSecretHeader) {
        if (providerSecretHeader == null || !providerSecretHeader.equals(providerSecret)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        basketService.deleteAllRewardItems();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Create a new basket
     */
    @GetMapping("/basket/new")
    UUID initializeBasket() {
        return basketService.createNewBasket();
    }

    /**
     * Query a basket by its id
     */
    @GetMapping("/basket")
    ResponseEntity<Basket> getBasket(@RequestHeader UUID basketId) throws BasketServiceException {
        var basket = basketService.getBasketById(basketId);
        var basketValue = basketService.getBasketValue(basket);
        basket.setValue(basketValue);

        return new ResponseEntity<>(basket, HttpStatus.OK);
    }

    /**
     * Delete a basket using its id
     */
    @DeleteMapping("/basket")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBasket(@RequestHeader UUID basketId) {
        basketService.removeBasketWithId(basketId);
    }

    /**
     * Adds #count many items with the matching itemId to the basket if not present. Overwrites the count if present.
     */
    @PutMapping("/basket/items")
    void putItem(@RequestBody PutItemRequest putItemRequest) throws BasketServiceException {
        if (putItemRequest.getCount() <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Count must be positive.");
        }

        basketService.setItemInBasket(putItemRequest.getBasketId(), putItemRequest.getItemId(), putItemRequest.getCount());
    }

    /**
     * Removes an item from the basket.
     */
    @DeleteMapping("/basket/items")
    void deleteItem(@RequestHeader UUID basketId, @RequestParam String itemId) throws BasketServiceException {
        basketService.deleteItemFromBasket(basketId, itemId);
    }

    /**
     * Sets a basket to paid.
     * TODO add hashcode for integrity? At which state was the basket paid? (Avoid race condition between payment add adding 'free' items to basket.
     */
    @PostMapping("/basket/pay")
    void payBasket(@RequestHeader("pay-secret") String clientPaySecret, @RequestHeader("basket-id") UUID basketId) throws BasketServiceException {
        if (!clientPaySecret.equals(paymentSecret)) {
            throw new BasketUnauthorizedException("You are not authorized to access '/basket/pay'!");
        }
        basketService.payBasket(basketId);
    }

    /**
     * Sets a basket to paid, TODO for development only.
     */
    @PostMapping("/basket/pay-dev")
    void payBasket(@RequestHeader("basket-id") UUID basketId) throws BasketServiceException {
        basketService.payBasket(basketId);
    }

    @PostMapping("/basket/lock")
    void lockBasket(@RequestBody UUID basketId) throws BasketServiceException {
        basketService.lockBasket(basketId);
    }

    /**
     * Sets a basket to redeemed and stores the redeem request.
     * The redeem amount is modelled as basket's value
     * Returns an error code if the basket cannot be redeemed.
     * The same request can be used multiple times to allow users to recover from network errors etc.
     * <p>
     */
    @PostMapping("/basket/redeem")
    void redeemBasket(@RequestHeader("redeem-secret") String clientRedeemSecret, @RequestBody RedeemBasketRequest redeemRequest) throws BasketServiceException {
        if (!clientRedeemSecret.equals(redeemSecret)) {
            throw new BasketUnauthorizedException("You are not authorized to access '/basket/redeem'!");
        }
        basketService.redeemBasket(redeemRequest.getBasketId(), redeemRequest.getRedeemRequest(), redeemRequest.getValue());
    }

    /**
     * Put rewards to basket
     *
     * @param clientRedeemSecret clients need this secret to authenticate themselves. Prohibit users from adding secrets
     * @param rewardIds          list of the ids of all rewards to add
     */
    @PostMapping("/basket/rewards")
    void addRewardsToBasket(@RequestHeader("redeem-secret") String clientRedeemSecret, @RequestHeader("basket-id") UUID basketId, @RequestBody List<String> rewardIds) throws BasketServiceException {
        if (!clientRedeemSecret.equals(redeemSecret)) {
            throw new BasketUnauthorizedException("You are not authorized to access '/basket/redeem'!");
        }
        basketService.addRewardsToBasket(basketId, rewardIds);
    }

    /*
     * Some default error handlers
     */

    @ResponseStatus(value = HttpStatus.NOT_FOUND,
            reason = "Basket not found!")
    @ExceptionHandler(BasketNotFoundException.class)
    public void handleBasketNotFoundException() {

    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST,
            reason = "Basket is paid and hence cannot be altered!")
    @ExceptionHandler(BasketPaidException.class)
    public void handleBasketPaidException() {

    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND,
            reason = "Shopping item not found!")
    @ExceptionHandler(ItemNotFoundException.class)
    public void handleItemNotFoundException() {

    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST,
            reason = "The sent basket value does math its actual value.")
    @ExceptionHandler(WrongBasketValueException.class)
    public void handleWrongBasketValueException() {

    }

    @ExceptionHandler(BasketUnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(BasketUnauthorizedException basketUnauthorizedException) {
        return new ResponseEntity<>(basketUnauthorizedException.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /*
     * Default handler for custom exceptions
     */
    @ExceptionHandler(BasketServiceException.class)
    public ResponseEntity<String> handleBasketServiceException(BasketServiceException basketServiceException) {
        return new ResponseEntity<>(basketServiceException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
