package de.upb.crypto.incentive.basketserver;

import de.upb.crypto.incentive.basketserver.exceptions.*;
import de.upb.crypto.incentive.basketserver.model.Basket;
import de.upb.crypto.incentive.basketserver.model.Item;
import de.upb.crypto.incentive.basketserver.model.requests.RedeemRequest;
import de.upb.crypto.incentive.basketserver.model.requests.PayRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

// TODO ensure basketIds are not transmitted via url

/**
 * A REST controller that defines and handles all requests of the basket server.
 */
@AllArgsConstructor
@RestController
public class BasketController {

    private BasketService basketService;  // Spring boot automatically injects a BasketService object

    /**
     * Can be used for health checking.
     */
    @GetMapping("/")
    String getHelloWorld() {
        return "Hello World";
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
    ResponseEntity<Basket> getBasket(@RequestParam UUID basketId) throws BasketServiceException {
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
    void deleteBasket(@RequestParam UUID basketId) {
        basketService.removeBasketWithId(basketId);
    }

    /**
     * Adds #count many items with the matching itemId to the basket if not present. Overwrites the count if present.
     */
    @PutMapping("/basket/items")
    void putItem(@RequestParam UUID basketId, @RequestParam UUID itemId, @RequestParam int count) throws BasketServiceException {
        if (count <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Count must be positive.");
        }

        basketService.setItemInBasket(basketId, itemId, count);
    }

    /**
     * Removes an item from the basket.
     */
    @DeleteMapping("/basket/items")
    void deleteItem(@RequestParam UUID basketId, @RequestParam UUID itemId) throws BasketServiceException {
        basketService.deleteItemFromBasket(basketId, itemId);
    }

    /**
     * Sets a basket to paid.
     * TODO: shared secret to prevent users from doing this. Send 403 to normal users
     * TODO add hashcode for integrity?
     */
    @PostMapping("/basket/pay")
    void payBasket(@RequestBody PayRequest payRequest) throws BasketServiceException {
        basketService.payBasket(payRequest.getBasketId(), payRequest.getValue());
    }

    /**
     * Sets a basket to redeemed and stores the redeem request.
     * The redeem amount is modelled as basket's value
     * Returns an error code if the basket cannot be redeemed.
     * The same request can be used multiple times to allow users to recover from network errors etc.
     * <p>
     * TODO: shared secret to prevent users from doing this. Send 403 to normal users
     */
    @PostMapping("/basket/redeem")
    void redeemBasket(@RequestBody RedeemRequest redeemRequest) throws BasketServiceException {
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

    /*
     * Default handler for custom exceptions
     */
    @ExceptionHandler(BasketServiceException.class)
    public ResponseEntity<String> handleBasketServiceException(BasketServiceException basketServiceException) {
        return new ResponseEntity<>(basketServiceException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
