<template>
    <div class="itemdiv">
        <h3 class="itemh3">{{item.title}} x{{item.count}}</h3>
        <p>Price: {{item.price}}</p>
    </div>
</template>

<script>
    export default {
        name: "ItemUI",
        props: {
            /*
            * The object representing the actual item data.
            * Consists of:
            * - name: text
            * - price: floating point number
            * - count: integer; how many exemplars of the item are in the basket
            */ 
            item: Object
        }
    }
</script>

<style>
    .itemdiv {
        background: #e6e6e6;
        margin: 5px;
        padding: 10px 20px;
    }

    .itemh3 {
        font-weight: bold;
    }
</style>