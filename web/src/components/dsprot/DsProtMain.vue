<template>
    <p>Double-spending Protection Service</p>
    {{helloMessage}}
    <TransactionList :transactions="transactions"/>
</template>

<script>
    import TransactionList from "./TransactionList.vue"

    export default {
        name: 'DsProtMain',
        props: {

        },
        components: {
            TransactionList
        },
        data() {
            return {
                /*
                * Declares empty transaction array, is filled at component creation time.
                * Invariant: contains all transactions that the system is currently aware of.
                */
                transactions: [],
                
                // message for heartbeat check of double-spending protection service (queried from backend where it is hard-coded)
                helloMessage: ''
            }
        },
        /*
        * Fills transactions array with the dummy data for now.
        * Later: connect to backend API (Spring Boot) to grab real transaction data.
        * Endpoint for that already coded, see GitHub issue #105.
        */
        async created() {
            // display heartbeat message to show server status
            const heartbeatRes = await fetch('dsprotection')
            this.helloMessage = await heartbeatRes.text()

            /*
            * Fetch transaction list (as array of JSON objects) from backend.
            *
            * Transaction is a triplet 
            * transaction ID, validity and reward that the user claimed with the respective spend transaction.
            */
            const transactionsRes = await fetch('dsprotection/transactions')
            this.transactions = await transactionsRes.json()
        }
    }
</script>

<style scoped>
    p {
        margin-top: 30px;
        text-align: center;
    }
</style>