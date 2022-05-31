<template>
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
                */
                transactions: []
            }
        },
        /*
        * Fills transactions array with the dummy data for now.
        * Later: connect to backend API (Spring Boot) to grab real transaction data.
        * Need to code an endpoint for that, see GitHub issue #105.
        */
        created() {
            // list of dummy transactions
            this.transactions = [
                /*
                * Transaction is a triplet 
                * transaction ID, validity and amount of points spent.
                * The spent amount is replaced with something describing the user's reward choice.
                */
                {
                    tid: '2176943540057753085382996214210019144198943853548817445801467093007984430190',
                    isValid: true,
                    spendAmount: 457
                },
                {
                    tid: '54654654673733734646316646735496764513528679485867548754875461242325456794578',
                    isValid: false,
                    spendAmount: 438
                },
                {
                    tid: '64646446575576567564576237231276659764637382373132416199546719751654637328',
                    isValid: false,
                    spendAmount: 462
                },
                {
                    tid: '3756567546516641537418357481379453165679537856345637645673576354673567436736',
                    isValid: true,
                    spendAmount: 478
                }
            ]
        }
    }
</script>

<style scoped>
    p {
        margin-top: 30px;
        text-align: center;
    }
</style>