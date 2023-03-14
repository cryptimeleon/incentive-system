<template>
  <div class="rounded shadow-md">
    <div class=" p-4">
      <div class="flex flex-row items-baseline justify-between">
        <div class="text-xl font-semibold">
          {{ item.title }}
        </div>
        <div>
          {{
            new Intl.NumberFormat('de-DE', {
              style: 'currency',
              currency: 'EUR'
            }).format((item.price) / 100)
          }}
        </div>
      </div>
      <VueBarcode :value="item.id" :options="{ format: 'EAN13', background: '#FFFFFF00' }">
      </VueBarcode>
    </div>
  </div>
</template>
<script>
import VueBarcode from '@chenfengyuan/vue-barcode'

export default {
  name: 'ShoppingItemCard',
  components: {VueBarcode},
  props: {
    item: {}
  }
}
</script>