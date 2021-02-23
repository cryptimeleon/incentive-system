package de.upb.crypto.incentive.basketserver;

import org.cryptimeleon.incentivesystem.basketserver.exceptions.*;
import org.cryptimeleon.incentivesystem.basketserver.model.Basket;
import org.cryptimeleon.incentivesystem.basketserver.model.Item;
import org.cryptimeleon.incentivesystem.basketserver.model.requests.PayBasketRequest;
import org.cryptimeleon.incentivesystem.basketserver.model.requests.PutItemRequest;
import org.cryptimeleon.incentivesystem.basketserver.model.requests.RedeemBasketRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;


/**
 * A REST controller that defines and handles all requests of the basket server.
 */
@RestController
public class BasketController {

    private BasketService basketService;  // Spring boot automatically injects a BasketService object

    @Value("${basketserver.pay-secret}")
    private String paymentSecret;

    @Value("${basketserver.redeem-secret}")
    private String redeemSecret;

    public BasketController(BasketService basketService, @Value("${basketserver.pay-secret}") String paymentSecret, @Value("${basketserver.redeem-secret}") String redeemSecret) {
        this.basketService = basketService;
        this.paymentSecret = paymentSecret;
        this.redeemSecret = redeemSecret;
    }

    /**
     * Can be used for health checking.
     */
    @GetMapping("/")
    String getHelloWorld() {
        return "Basketserver";
    }


    /**
     * Query all shopping items that can be purchased
     */
    @GetMapping("/items")
    Item[] getAllBasketItems() {
        return basketService.getItems();
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
    void deleteItem(@RequestHeader UUID basketId, @RequestParam UUID itemId) throws BasketServiceException {
        basketService.deleteItemFromBasket(basketId, itemId);
    }

    /**
     * Sets a basket to paid.
     * TODO add hashcode for integrity?
     */
    @PostMapping("/basket/pay")
    void payBasket(@RequestHeader("pay-secret") String clientPaySecret, @RequestBody PayBasketRequest payBasketRequest) throws BasketServiceException {
        if (!clientPaySecret.equals(paymentSecret)) {
            throw new BasketUnauthorizedException("You are not authorized to access '/basket/pay'!");
        }
        basketService.payBasket(payBasketRequest.getBasketId(), payBasketRequest.getValue());
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
