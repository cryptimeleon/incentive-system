<template>

</template>

<script>
    export default {
        name: 'BasketUI',
        props: {
            /*
            * The object representing the actual basket data.
            * Consists of:
            * - basketID: unique identifier of the basket
            * - items: list of items contained in the basket
            * - basketItems: list of reward items the user claims for this basket
            * - paid: flag indicating whether this basket has been paid
            * - locked: flag indicating whether this basket has been locked 
            *       (i.e. no more items can be added to it)
            * - redeemed: flag indicating whether user has already claimed their reward
            * The paid/locked/redeemed flags alter the color of the div representing the Basket component.
            */
            basket: Object
        }
    }
</script>