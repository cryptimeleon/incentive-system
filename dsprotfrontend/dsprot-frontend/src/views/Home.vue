<template>
    {{helloMessage}}
    <TransactionList :transactions="transactions"/>
</template>

<script>
    import TransactionList from "../components/TransactionList.vue"

    export default {
        name: 'Home',
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
                
                // hard-coded message for heartbeat check of double-spending protection service
                helloMessage: ''
            }
        },
        /*
        * Fills transactions array with the dummy data for now.
        * Later: connect to backend API (Spring Boot) to grab real transaction data.
        * Endpoint for that already coded, see GitHub issue #105.
        */
        async created() {
            /*
            * Fetch transaction list (as array of JSON objects) from backend.
            *
            * Transaction is a triplet 
            * transaction ID, validity and reward that the user claimed with the respective spend transaction.
            */
            const transactionsRes = await fetch('api/transactions')
            this.transactions = await transactionsRes.json()

            // display heartbeat message to show server status
            const heartbeatRes = await fetch('api')
            this.helloMessage = await heartbeatRes.text()
        }
    }
</script>

<style scoped>
    p {
        margin-top: 30px;
        text-align: center;
    }
</style>