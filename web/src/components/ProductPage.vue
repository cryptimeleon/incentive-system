<template>
  <div class="w-full">
    <div class="flex flex-row space-x-4">
      <div class="text-3xl font-bold">Shopping Items</div>
    </div>
    <div class="prose text-lg pb-2">
      We provide you with the barcodes of shopping items to try out the app at home.
    </div>
    <div class="grid md:grid-cols-2 grid-cols-1 gap-x-8 gap-y-12 pb-8">
      <ShoppingItemCard v-for="item in shoppingItems" :key="item.id" :item="item"/>
    </div>
  </div>
</template>

<script>
import ShoppingItemCard from "@/components/ShoppingItemCard.vue";

export default {
  name: "ProductPage",
  components: {
    ShoppingItemCard,
  },
  data() {
    return {
      shoppingItems: [
        {
          "id": "4008400404127",
          "title": "Hazelnut Spread",
          "price": 239
        },
        {
          "id": "4001257000122",
          "title": "Green Tea",
          "price": 289
        },
        {
          "id": "8718951312432",
          "title": "Colgate Zahnpasta",
          "price": 199
        }
      ],
    };
  },

}
</script>
