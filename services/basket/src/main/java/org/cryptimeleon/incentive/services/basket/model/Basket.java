package org.cryptimeleon.incentive.services.basket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Model class representing a basket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Basket {
    @ApiModelProperty(value = "${basketModel.basketID}")
    private UUID basketID; // unique identifier for the basket

    @ApiModelProperty(value = "${basketModel.items}")
    private Map<String, Integer> items; // list of items that are contained in the basket

    @ApiModelProperty(value = "${basketModel.rewardItems}")
    private List<String> rewardItems; // the list of reward items that the user gets once she redeems the basket

    @ApiModelProperty(value = "${basketModel.paid}")
    private boolean paid; // whether the basket has already been paid

    /*
    * Whether the user has already executed the spend and earn requests associated with this basket,
    * i.e. whether she has already received the points and rewards for it.
    */
    @ApiModelProperty(value = "${basketModel.redeemed}")
    private boolean redeemed;

    /*
    * Baskets are locked before they are paid/redeemed
    * which means that no more items can be added to them.
    */
    @ApiModelProperty(value = "${basketModel.locked}")
    private boolean locked;

    /*
    * The first redeem request that the user sent for this basket is stored.
    * This allows for the user to safely retry redeeming in case of connectivity problems
    * without allowing for easy double-spending attacks.
    */
    @ApiModelProperty(value = "${basketModel.redeemRequest}")
    private String redeemRequest;

    @ApiModelProperty(value = "${basketModel.value}")
    private long value; // value must be set manually for serialization

    /**
     * Creates a new basket with the passed id.
     * All other attributes are set to "empty" default values.
     */
    public Basket(UUID id) {
        basketID = id;
        items = new HashMap<>();
        rewardItems = new ArrayList<>();
        paid = false;
        locked = false;
        redeemed = false;
        redeemRequest = "";
    }
}
