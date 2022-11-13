<template>
    <div :key="basket.basketID" v-for="basket in baskets">
        <BasketUI :basket="basket" />
    </div>
</template>

<script>
    import BasketUI from "./Basket.vue"

    export default {
        name: 'BasketList',
        props: {
            baskets: Array
        },
        components: {
            BasketUI
        }
    }
</script>