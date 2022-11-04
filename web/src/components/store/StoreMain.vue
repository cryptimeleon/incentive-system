<template> 
    <!-- Main component of the store frontend. 
        Displays a basket list. -->
    <p>Hello, this is a friendly store frontend!</p>
    {{helloMessage}}
    <BasketList :baskets="baskets"/>
</template>

<script>
    import BasketList from "./BasketList.vue"

    export default {
        name: 'StoreMain',
        props: {
            
        },
        components: {
            BasketList
        },
        data() {
            return {
                /*
                * Declares empty basket array.
                * Intially empty, filled at component creation time (in created method).
                * Invariant: contains all baskets that the system is currently aware of.
                */
                baskets: [],

                // message for heartbeat check of basket service (queried from backend where it is hard-coded)
                helloMessage: ''
            }
        },
        /*
        * Connects to backend (= basket server) to fill the baskets array.
        * Monitors server state by displaying heartbeat message.
        */
        async created() {
            // display heartbeat message to show server status
            const heartbeatRes = await fetch('basket')
            this.helloMessage = await heartbeatRes.text()

            // create dummy basket list
            this.baskets = [
                {
                    basketID: 426,
                    items: [
                        {
                            name: "Awesome new flying toaster",
                            price: 42.60,
                            count: 426
                        }
                    ]
                },
                {
                    basketID: 808,
                    items: [
                        {
                            name: "Potato chips",
                            price: 4.26,
                            count: 426
                        },
                        {
                            name: "Perfect apple",
                            price: 4.26,
                            count: 426
                        }
                    ]
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