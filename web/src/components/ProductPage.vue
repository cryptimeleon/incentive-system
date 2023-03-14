<template>
  <div class="w-full">
    <div class="flex flex-row space-x-4">
      <div class="text-3xl font-bold">Shopping Items</div>
    </div>
    <div class="prose text-lg pb-2">
      We provide you with the barcodes of shopping items to try out the app at home.
    </div>
    <div class="grid md:grid-cols-2 grid-cols-1 gap-8 pb-8">
      <div v-for="item in shoppingItems" :key="item.id">
        <ShoppingItemCard :item="item"/>
      </div>
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
      shoppingItems: [],
    };
  },
  async created() {
    fetch("/basket/items")
        .then(response => {
          if (!response.ok) throw Error(response.statusText)
          return response
        })
        .then(response => response.json())
        .then(data => this.shoppingItems = data)
        .catch(error => console.error(error))
  }
}
</script>
