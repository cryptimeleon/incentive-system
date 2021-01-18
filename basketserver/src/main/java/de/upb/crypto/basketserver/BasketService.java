package de.upb.crypto.basketserver;

import de.upb.crypto.basketserver.exceptions.*;
import de.upb.crypto.basketserver.model.Basket;
import de.upb.crypto.basketserver.model.BasketItem;
import de.upb.crypto.basketserver.model.Item;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BasketService {

    private HashMap<UUID, Basket> basketMap = new HashMap<>();
    private ArrayList<Item> items = new ArrayList<>(
            Arrays.asList(
                    new Item(UUID.fromString("b363b7fa-14b9-402b-98b2-6e7370d62595"),
                            "Tomato",
                            30),
                    new Item(
                            UUID.fromString("1f360b5f-f458-4f42-af93-50fbe8a68846"),
                            "Apple",
                            50),
                    new Item(UUID.fromString("a785cdb7-eac4-4b28-908b-2ba18944a79e"),
                            "Peach",
                            30),
                    new Item(UUID.fromString("c237b2bc-9f71-4673-bb8f-09fb477e71ba"), "Potatoes", 150),
                    new Item(UUID.fromString("06e64293-acd0-43fc-824d-7600bb6a1fa7"), "Mango", 90)
            ));
    private Map<UUID, Item> itemMap = items.stream().collect(Collectors.toMap(Item::getId, Function.identity()));


    // Value field would be redundant and computational overhead should be small
    private int getBasketValue(Basket basket) {
        return getBasketItemsInBasket(basket)
                .mapToInt((basketItem) -> basketItem.getItem().getPrice() * basketItem.getCount())
                .sum();
    }

    private boolean canBasketBeAltered(Basket basket) {
        return !basket.isPaid() && !basket.isRedeemed();
    }

    Stream<BasketItem> getBasketItemsInBasket(Basket basket) {
        return basket
                .getItems()
                .entrySet()
                .stream()
                .map((e) -> new BasketItem(itemMap.get(e.getKey()), e.getValue()));
    }

    Optional<Basket> getBasketById(UUID basketId) {
        return Optional.ofNullable(basketMap.get(basketId));
    }

    void removeBasketWithId(UUID basketId) {
        basketMap.remove(basketId);
    }

    UUID createNewBasket() {
        var id = UUID.randomUUID();
        basketMap.put(id, new Basket(id));  // probability of same id negligible
        return id;
    }

    private boolean hasItem(UUID itemId) {
        return itemMap.containsKey(itemId);
    }

    Optional<Item> getItemById(UUID itemId) {
        return Optional.ofNullable(itemMap.get(itemId));
    }

    public void setItemInBasket(UUID basketId, UUID itemId, int count) throws BasketServiceException {
        assert count > 0;

        var basketOptional = getBasketById(basketId);

        if (basketOptional.isEmpty()) throw new BasketNotFoundException();
        if (!canBasketBeAltered(basketOptional.get())) throw new BasketPaidException();
        if (!hasItem(itemId)) throw new ItemNotFoundException();

        basketOptional.get().getItems().put(itemId, count);
    }

    public void deleteItemFromBasket(UUID basketId, UUID itemId) throws BasketServiceException {
        var basket = getBasketById(basketId);

        if (basket.isEmpty()) throw new BasketNotFoundException();
        if (!canBasketBeAltered(basket.get())) throw new BasketPaidException();

        basket.get().getItems().remove(itemId);
    }

    public void payBasket(UUID basketId, int value) throws BasketServiceException {
        var basket = getBasketById(basketId);
        if (basket.isEmpty()) throw new BasketNotFoundException();

        var basketValue = getBasketValue(basket.get());
        if (value != basketValue) throw new WrongBasketValueException();

        basket.get().setPaid(true);
    }

    public boolean isBasketPaid(UUID basketId) throws BasketNotFoundException {
        var basket = getBasketById(basketId);
        if (basket.isEmpty()) throw new BasketNotFoundException();

        return basket.get().isPaid();
    }

    public void redeemBasket(UUID basketId, String redeemRequest, int value) throws BasketServiceException {
        var basket = getBasketById(basketId);

        if (basket.isEmpty()) throw new BasketNotFoundException();
        if (!basket.get().isPaid()) throw new BasketServiceException("Basket not paid!");
        if (getBasketValue(basket.get()) != value) throw new WrongBasketValueException();
        if (basket.get().isRedeemed() && !basket.get().getRedeemRequest().equals(redeemRequest))
            throw new BasketServiceException("Basket id already redeemed!");
        if (basket.get().isRedeemed() && basket.get().getRedeemRequest().equals(redeemRequest))
            return;  // Same request Can be used twice bacause it yields the same token

        basket.get().setRedeemed(true);
        basket.get().setRedeemRequest(redeemRequest);
    }
}

