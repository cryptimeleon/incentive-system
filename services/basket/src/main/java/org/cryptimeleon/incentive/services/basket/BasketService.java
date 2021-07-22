package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.services.basket.exceptions.*;
import org.cryptimeleon.incentive.services.basket.model.Basket;
import org.cryptimeleon.incentive.services.basket.model.BasketItem;
import org.cryptimeleon.incentive.services.basket.model.Item;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service containing the business logic of the basket server.
 * Currently simulating the behavior of a database with in-memory objects
 */
@Service
public class BasketService {

    private final HashMap<UUID, Basket> basketMap;
    private final ArrayList<Item> items;
    private final Map<String, Item> itemMap;

    /**
     * Initialize basket service with some shopping items
     */
    BasketService() {
        basketMap = new HashMap<>();
        items = new ArrayList<>(
                Arrays.asList(
                        new Item(
                                "3941288190038",
                                "Sweetened hazelnut cocoa spread",
                                199),
                        new Item(
                                "1022525418053",
                                "Tomato",
                                30),
                        new Item(
                                "4621006331880",
                                "Apple",
                                50),
                        new Item(
                                "4536852654932",
                                "Peach",
                                30),
                        new Item(
                                "2936746557615",
                                "Potatoes",
                                150),
                        new Item(
                                "0680818152421",
                                "Mango",
                                90)
                ));
        itemMap = items.stream().collect(Collectors.toMap(Item::getId, Function.identity()));
    }

    public Item[] getItems() {
        return items.toArray(new Item[0]);
    }

    public UUID createNewBasket() {
        var id = UUID.randomUUID();
        basketMap.put(id, new Basket(id));  // probability of same id negligible
        return id;
    }

    public Basket getBasketById(UUID basketId) throws BasketNotFoundException {
        var basketOptional = Optional.ofNullable(basketMap.get(basketId));
        if (basketOptional.isEmpty()) throw new BasketNotFoundException();
        return basketOptional.get();
    }

    public void removeBasketWithId(UUID basketId) {
        basketMap.remove(basketId);
    }

    public void setItemInBasket(UUID basketId, String itemId, int count) throws BasketServiceException {
        assert count > 0;

        var basket = getBasketById(basketId);

        if (isBasketImmutable(basket)) throw new BasketPaidException();
        if (!hasItem(itemId)) throw new ItemNotFoundException();

        basket.getItems().put(itemId, count);
    }

    public void deleteItemFromBasket(UUID basketId, String itemId) throws BasketServiceException {
        var basket = getBasketById(basketId);

        if (isBasketImmutable(basket)) throw new BasketPaidException();

        basket.getItems().remove(itemId);
    }

    public void payBasket(UUID basketId, long value) throws BasketServiceException {
        var basket = getBasketById(basketId);

        long basketValue = getBasketValue(basket);
        if (value != basketValue) throw new WrongBasketValueException();
        if (basket.getItems().isEmpty()) throw new BasketServiceException("Cannot pay empty baskets");

        basket.setPaid(true);
    }

    public void redeemBasket(UUID basketId, String redeemRequest, long value) throws BasketServiceException {
        var basket = getBasketById(basketId);

        if (!basket.isPaid()) throw new BasketServiceException("Basket not paid!");
        if (getBasketValue(basket) != value) throw new WrongBasketValueException();
        if (basket.isRedeemed() && !basket.getRedeemRequest().equals(redeemRequest))
            throw new BasketServiceException("Basket id already redeemed!");
        if (basket.isRedeemed() && basket.getRedeemRequest().equals(redeemRequest))
            return;  // Same request Can be used twice because it yields the same token

        basket.setRedeemed(true);
        basket.setRedeemRequest(redeemRequest);
    }

    /*
     * Function for computing the value of a basket.
     * Basket does not know its value to prevent redundant data / weird data ownership.
     */
    public long getBasketValue(Basket basket) {
        return getBasketItemsInBasket(basket)
                .mapToLong((basketItem) -> basketItem.getItem().getPrice() * basketItem.getCount())
                .sum();
    }

    private boolean isBasketImmutable(Basket basket) {
        return basket.isPaid();
    }

    private Stream<BasketItem> getBasketItemsInBasket(Basket basket) {
        return basket
                .getItems()
                .entrySet()
                .stream()
                .map((e) -> new BasketItem(itemMap.get(e.getKey()), e.getValue()));
    }

    private boolean hasItem(String itemId) {
        return itemMap.containsKey(itemId);
    }

    public Item getItem(String id) {
        return itemMap.get(id);
    }
}

