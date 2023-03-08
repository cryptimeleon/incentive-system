<template>
    <div class="w-full">
        <div class="flex flex-row space-x-4">
            <div class="text-3xl font-bold">{{ name }}</div>
            <ServiceStatus :loading="loading" :online="online"/>
        </div>
        <div class="prose text-lg">{{ description }}</div>
        <div class="text-2xl font-bold pt-2">Baskets</div>
        <BasketList :baskets="baskets"/>
    </div>
</template>

<script>
import BasketList from "./BasketList.vue"
import ServiceStatus from "@/components/ServiceStatus.vue";

export default {
    name: 'StoreMain',
    props: {
        baseUrl: String,
        name: String,
        description: String
    },
    components: {
        ServiceStatus,
        BasketList
    },
    data() {
        return {
            /*
            * Declares empty basket array.
            * Intially empty, filled at component creation time (in created method).
            * Invariant: contains all baskets that the system is currently aware of.
            */
            baskets: [{"basketID":"e941b709-bae2-47c1-b101-fbd5eb5c93c4","basketItems":[{"id":"4008400404127","title":"Hazelnut Spread","price":239,"count":5}],"rewardItems":[],"paid":true,"locked":true,"value":1195},{"basketID":"520b8bed-07be-4a9e-92fe-95d7cffc36cc","basketItems":[{"id":"4008400404127","title":"Hazelnut Spread","price":239,"count":7}],"rewardItems":[],"paid":true,"locked":true,"value":1673},{"basketID":"244d6cd5-21d6-4d69-ac30-d5589e506cd1","basketItems":[{"id":"4001257000122","title":"Green Tea","price":289,"count":1}],"rewardItems":[{"id":"160859564846","title":"Hazelnut Spread"}],"paid":true,"locked":true,"value":289},{"basketID":"d6721e5b-3fb3-45e3-832d-f1b6d6d55d7e","basketItems":[{"id":"4001257000122","title":"Green Tea","price":289,"count":1},{"id":"8718951312432","title":"Colgate Zahnpasta","price":199,"count":1}],"rewardItems":[],"paid":true,"locked":true,"value":488},{"basketID":"1847ab76-d9c4-4e8e-85b7-2d71e1077251","basketItems":[{"id":"4001257000122","title":"Green Tea","price":289,"count":1},{"id":"8718951312432","title":"Colgate Zahnpasta","price":199,"count":1}],"rewardItems":[],"paid":true,"locked":true,"value":488},{"basketID":"86ca301f-61d8-4288-b0e7-d17ca259b5f3","basketItems":[{"id":"4001257000122","title":"Green Tea","price":289,"count":1},{"id":"4008400404127","title":"Hazelnut Spread","price":239,"count":5},{"id":"8718951312432","title":"Colgate Zahnpasta","price":199,"count":1}],"rewardItems":[{"id":"160859564846","title":"Hazelnut Spread"}],"paid":true,"locked":true,"value":1683}],
            // message for heartbeat check of basket service (queried from backend where it is hard-coded)
            helloMessage: '',
            loading: true,
            online: false
        }
    },
    created() {
        this.$watch(
                () => this.$route.params,
                () => {
                    this.fetchData()
                },
                // fetch the data when the view is created and the data is
                // already being observed
                {immediate: true}
        )
    },
    methods: {
        fetchData() {
            const basePath = this.baseUrl
            // display heartbeat message to show server status
            fetch(basePath)
                    .then(response => {
                        this.online = response.ok
                        this.loading = false
                    })

            // create dummy basket list
            fetch(basePath + "/allbaskets")
                    .then(response => {
                        if (!response.ok) throw Error(response.statusText)
                        return response
                    })
                    .then(response => response.json())
                    // .then(data => this.baskets = data)
                    .catch(error => console.error(error))
        }
    },
}
</script>

<style scoped>
    p {
        margin-top: 30px;
        text-align: center;
    }
</style>