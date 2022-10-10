<template>
    <div>
        <h3>ID: {{basket.basketID}}</h3>
        <ItemList/>
    </div>
</template>

<script>
    import ItemList from "./ItemList.vue"

    export default {
    name: "BasketUI",
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
    },
    components: { ItemList }
}
</script>

<style scoped>
    h3 {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    p {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    div {
        background: #f4f4f4;
        margin: 5px;
        padding: 10px 20px;
        border-left: 15px solid black;
    }
</style>