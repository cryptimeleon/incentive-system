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
        <ShoppingItemCard :item="item" v-on:click="openShoppingItem(item)"/>
      </div>
    </div>
  </div>

  <div v-if="selectedItem!=null" class="fixed top-0 left-0 right-0 z-50 w-full bg-white/[.90] h-full flex justify-center items-center">
    <div class="relative max-w-fit">
      <OnClickOutside @trigger="hideShoppingItem" >
        <ShoppingItemCard :item="selectedItem"/>
      </OnClickOutside>
    </div>
  </div>
</template>

<script>
import ShoppingItemCard from "@/components/ShoppingItemCard.vue";
import { OnClickOutside } from '@vueuse/components'


export default {
  name: "ProductPage",
  components: {
    ShoppingItemCard,
    OnClickOutside
  },
  data() {
    return {
      shoppingItems: [],
      selectedItem: null,
    };
  },
  methods: {
    openShoppingItem(item) {
      console.log("Item selected")
      this.selectedItem = item
    },
    hideShoppingItem (event) {
      console.log("hide item: " + event)
      this.selectedItem = null
    }
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
