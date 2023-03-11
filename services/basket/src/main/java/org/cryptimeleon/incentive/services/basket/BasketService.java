package org.cryptimeleon.incentive.services.basket;

import org.cryptimeleon.incentive.services.basket.exceptions.BasketNotFoundException;
import org.cryptimeleon.incentive.services.basket.exceptions.BasketPaidException;
import org.cryptimeleon.incentive.services.basket.exceptions.BasketServiceException;
import org.cryptimeleon.incentive.services.basket.exceptions.ItemNotFoundException;
import org.cryptimeleon.incentive.services.basket.storage.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service containing the business logic of the basket server.
 * Currently simulating the behavior of a database with in-memory objects
 */
@Service
public class BasketService {

    private final RewardItemRepository rewardItemRepository;
    private final ItemRepository itemRepository;
    private final BasketRepository basketRepository;

    /**
     * Initialize basket service with empty shopping item list.
     */
    BasketService(ItemRepository itemRepository, BasketRepository basketRepository, RewardItemRepository rewardItemRepository) {
        this.rewardItemRepository = rewardItemRepository;
        this.itemRepository = itemRepository;
        this.basketRepository = basketRepository;
    }

    /**
     * Returns a list of all shopping items that can be purchased.
     */
    public List<ItemEntity> getItems() {
        return StreamSupport.stream(itemRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    /**
     * Returns true if and only if the basket service has a purchasable item with the passed ID.
     *
     * @param itemId id of the item to search for
     * @return true if item exists
     */
    private boolean hasItem(String itemId) {
        return itemRepository.existsById(itemId);
    }

    /**
     * Returns the purchasable item with the passed ID.
     */
    public Optional<ItemEntity> getItem(String id) {
        return itemRepository.findById(id);
    }

    /**
     * Makes the passed item a new purchasable item (registered under its ID).
     */
    public void save(ItemEntity item) {
        itemRepository.save(item);
    }

    /**
     * Deletes all purchasable items.
     */
    public void deleteAllItems() {
        itemRepository.deleteAll();
    }

    public List<RewardItemEntity> getRewardItems() {
        return StreamSupport.stream(rewardItemRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public void deleteAllRewardItems() {
        rewardItemRepository.deleteAll();
    }

    public void save(RewardItemEntity rewardItemEntity) {
        var itemOptional = rewardItemRepository.findById(rewardItemEntity.getId());
        if (itemOptional.isEmpty()) {
            rewardItemRepository.save(rewardItemEntity);
        }
    }

    public UUID createNewBasket() {
        BasketEntity basketEntity = new BasketEntity();
        basketRepository.save(basketEntity);
        return basketEntity.getBasketID();
    }

    public BasketEntity getBasketById(UUID basketId) throws BasketNotFoundException {
        var basketOptional = basketRepository.findById(basketId);
        if (basketOptional.isEmpty()) throw new BasketNotFoundException();
        return basketOptional.get();
    }

    /**
     * Returns a list of all baskets that are in the system.
     *
     * @return ArrayList
     */
    public List<BasketEntity> getAllBaskets() {
        return (ArrayList<BasketEntity>) basketRepository.findAll();
    }

    public void removeBasketWithId(UUID basketId) {
        basketRepository.deleteById(basketId);
    }

    public void setItemInBasket(UUID basketId, String itemId, int count) throws BasketServiceException {
        assert count > 0;

        var basket = getBasketById(basketId);
        var item = getItem(itemId).orElseThrow(ItemNotFoundException::new);

        if (isBasketImmutable(basket)) throw new BasketPaidException();
        if (!hasItem(itemId)) throw new ItemNotFoundException();

        basket.addBasketItem(item, count);
        basketRepository.save(basket);
    }

    public void deleteItemFromBasket(UUID basketId, String itemId) throws BasketServiceException {
        var basket = getBasketById(basketId);

        if (isBasketImmutable(basket)) throw new BasketPaidException();

        var item = getItem(itemId).orElseThrow();
        basket.removeBasketItem(item);
        basketRepository.save(basket);
    }

    public void payBasket(UUID basketId) throws BasketServiceException {
        var basket = getBasketById(basketId);
        if (basket.getBasketItems().isEmpty()) throw new BasketServiceException("Cannot pay empty baskets");
        basket.setPaid(true);
        basketRepository.save(basket);
    }

    public void lockBasket(UUID basketId) throws BasketServiceException {
        var basket = getBasketById(basketId);
        if (basket.getBasketItems().isEmpty()) throw new BasketServiceException("Cannot pay empty baskets");
        basket.setLocked(true);
        basketRepository.save(basket);
    }

    private boolean isBasketImmutable(BasketEntity basket) {
        return basket.isPaid();
    }
}
