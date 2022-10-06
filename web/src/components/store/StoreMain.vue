<template> 
    <!-- Main component of the store frontend. 
        Displays a basket list. -->
    <p>Hello, this is a friendly store frontend!</p>
    <BasketList/>
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
                baskets: []
            }
        }
    }
</script>