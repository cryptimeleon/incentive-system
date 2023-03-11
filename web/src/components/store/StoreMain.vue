<template>
    <div class="w-full">
        <div class="flex flex-row space-x-4">
            <div class="text-3xl font-bold">{{ name }}</div>
            <ServiceStatus :loading="loading" :online="online"/>
        </div>
        <div class="prose text-lg">{{ description }}</div>
        <div class="text-2xl font-bold pt-2">Baskets</div>
        <BasketList :baskets="basketWithDsData"/>
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
            * Initially empty, filled at component creation time (in created method).
            * Invariant: contains all baskets that the system is currently aware of.
            */
            baskets: [],
            dsData: [],
            // message for heartbeat check of basket service (queried from backend where it is hard-coded)
            helloMessage: '',
            loading: true,
            online: false
        }
    },
    computed: {
        basketWithDsData() {
            return this.baskets.map(basket => {
                let dsData = this.dsData.find(data => data.basketId === basket.basketID)
                if (dsData!==undefined) {
                    basket.dsData = dsData
                }
                return basket
            });
        }
    },
    watch: {
        baseUrl: {
            handler(newBaseUrl) {
                // display heartbeat message to show server status
                fetch(newBaseUrl)
                        .then(response => {
                            this.online = response.ok
                            this.loading = false
                        })

                // create dummy basket list
                fetch(newBaseUrl + "/allbaskets")
                        .then(response => {
                            if (!response.ok) throw Error(response.statusText)
                            return response
                        })
                        .then(response => response.json())
                        .then(data => this.baskets = data)
                        .catch(error => console.error(error))

                fetch("/incentive/double-spending-detected")
                        .then(response => {
                            if (!response.ok) throw Error(response.statusText)
                            return response
                        })
                        .then(response => response.json())
                        .then(data => this.dsData = data)
                        .catch(error => console.error(error))
            },
            immediate: true
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