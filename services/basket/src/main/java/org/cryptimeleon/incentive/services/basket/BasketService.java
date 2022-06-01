package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.services.basket.exceptions.*;
import org.cryptimeleon.incentive.services.basket.model.Basket;
import org.cryptimeleon.incentive.services.basket.model.BasketItem;
import org.cryptimeleon.incentive.services.basket.model.Item;
import org.cryptimeleon.incentive.services.basket.model.RewardItem;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

/**
 * Service containing the business logic of the basket server.
 * Currently simulating the behavior of a database with in-memory objects
 */
@Service
public class BasketService {

    private final HashMap<UUID, Basket> basketMap;
    private final List<RewardItem> rewardItems;
    private final Map<String, Item> itemMap;

    /**
     * Initialize basket service with some shopping items
     */
    BasketService() {
        this.rewardItems = new ArrayList<>(Arrays.asList(
        ));
        basketMap = new HashMap<>();
        itemMap = new HashMap<>();
    }

    public Item[] getItems() {
        return itemMap.values().toArray(new Item[0]);
    }

    private boolean hasItem(String itemId) {
        return itemMap.containsKey(itemId);
    }

    public Item getItem(String id) {
        return itemMap.get(id);
    }

    public void save(Item item) {
        itemMap.put(item.getId(), item);
        System.out.println(itemMap.containsKey(item.getId()));
    }

    public void deleteAllItems() {
        itemMap.clear();
    }

    public RewardItem[] getRewardItems() {
        return rewardItems.toArray(new RewardItem[0]);
    }

    public void deleteAllRewardItems() {
        rewardItems.clear();
    }

    public void save(RewardItem rewardItem) {
        if (!rewardItems.contains(rewardItem)) {
            rewardItems.add(rewardItem);
        }
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

    public void addRewardsToBasket(UUID basketId, List<String> rewardItemIds) throws BasketServiceException {
        var basket = getBasketById(basketId);

        if (isBasketImmutable(basket)) throw new BasketPaidException();

        basket.setRewardItems(rewardItemIds);
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

    public void payBasket(UUID basketId) throws BasketServiceException {
        var basket = getBasketById(basketId);
        if (basket.getItems().isEmpty()) throw new BasketServiceException("Cannot pay empty baskets");
        basket.setPaid(true);
    }

    public void lockBasket(UUID basketId) throws BasketServiceException {
        var basket = getBasketById(basketId);
        if (basket.getItems().isEmpty()) throw new BasketServiceException("Cannot pay empty baskets");
        basket.setLocked(true);
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
}

