<template>
    <div class="itemdiv">
        <h3>{{item.name}} x{{item.count}}</h3>
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
        background: #d5f8ce;
        margin: 5px;
        padding: 10px 20px;
    }
</style>