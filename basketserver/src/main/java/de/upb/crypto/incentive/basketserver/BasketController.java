package de.upb.crypto.incentive.basketserver;

import de.upb.crypto.incentive.basketserver.exceptions.BasketNotFoundException;
import de.upb.crypto.incentive.basketserver.exceptions.BasketServiceException;
import de.upb.crypto.incentive.basketserver.model.Basket;
import de.upb.crypto.incentive.basketserver.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class BasketController {


    Logger logger = LoggerFactory.getLogger(BasketController.class);

    @Autowired
    BasketService basketService;


    // TODO ensure basketIds are not transmitted via url

    @GetMapping("/")
    String getHelloWorld() {
        return "Hello World";
    }

    @GetMapping("/basket")
    ResponseEntity<Basket> getBasket(@RequestParam UUID basketId) {
        var basketOptional = basketService.getBasketById(basketId);
        if (basketOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Basket not found.");
        }

        var basket = basketOptional.get();
        var basketValue = basketService.getBasketValue(basket);
        basket.setValue(basketValue);

        return new ResponseEntity<>(basket, HttpStatus.OK);
    }

    @DeleteMapping("/basket")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBasket(@RequestParam UUID basketId) {
        basketService.removeBasketWithId(basketId);
    }

    @GetMapping("/basket/new")
    UUID initializeBasket() {
        return basketService.createNewBasket();
    }

    @PutMapping("/basket/items")
    void putItem(@RequestParam UUID basketId, @RequestParam UUID itemId, @RequestParam int count) {
        if (count <= 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Count must be positive.");
        }

        try {
            basketService.setItemInBasket(basketId, itemId, count);
        } catch (BasketServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/basket/items")
    void deleteItem(@RequestParam UUID basketId, @RequestParam UUID itemId) {
        try {
            basketService.deleteItemFromBasket(basketId, itemId);
        } catch (BasketServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/basket/pay")
    void payBasket(@RequestParam UUID basketId, @RequestParam int value) {
        // TODO add hashcode for integrity?
        try {
            basketService.payBasket(basketId, value);
        } catch (BasketNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BasketServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/basket/redeem")
        // Redeem amount modelled as basket's value
    void redeemBasket(@RequestParam UUID basketId, @RequestParam String redeemRequest, @RequestParam int value) {
        try {
            basketService.redeemBasket(basketId, redeemRequest, value);
        } catch (BasketServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/items")
    Item[] getAllBasketItems() {
        return basketService.getItems();
    }
}
