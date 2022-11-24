<template>
    <div :key="ta.tid" v-for="ta in transactions">
        <TransactionUI :transaction="ta" />
    </div>
</template>

<script>
    import TransactionUI from "./Transaction.vue"

    export default {
        name: 'TransactionList',
        props: {
            transactions: Array
        },
        components: {
            TransactionUI
        }
    }
</script>