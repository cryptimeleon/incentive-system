<template>
    <div :key="ta.tid" v-for="ta in transactions">
        <Transaction :transaction="ta" />
    </div>
</template>

<script>
    import Transaction from "./Transaction.vue"

    export default {
        name: 'TransactionList',
        props: {
            transactions: Array
        },
        components: {
            Transaction
        }
    }
</script>  