<template>
    <div class="flex flex-col pb-8 gap-4">
        <BasketCard :basket="basket" v-for="basket in baskets" :key="basket.basketID"/>
    </div>
</template>

<script>
import BasketCard from "@/components/store/BasketCard.vue";

export default {
    name: 'BasketList',
    components: {BasketCard},
    props: {
        baskets: Array,
    },
}
</script>