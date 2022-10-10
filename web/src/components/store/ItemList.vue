<template>
    <div :key="item.name" v-for="item in items">
        <ItemUI :item="item" />
    </div>
</template>

<script>
    import ItemUI from "./Item.vue"

    export default {
        name: 'ItemList',
        props: {
            items: Array
        },
        components: {
            ItemUI
        }
    }
</script>