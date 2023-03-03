<template>
    <div class="w-full">
        <div class="flex flex-row space-x-4">
            <div class="text-3xl font-bold">{{ name }}</div>
            <ServiceStatus v-if="!loading" :online="online"/>
        </div>
        <div class="prose text-lg">{{ description }}</div>

        {{ helloMessage }}
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
            baskets: [],

            // message for heartbeat check of basket service (queried from backend where it is hard-coded)
            helloMessage: '',
            loading: true,
            online: false
            }
        },
        /*
        * Connects to backend (= basket server) to fill the baskets array.
        * Monitors server state by displaying heartbeat message.
        */
        async created() {
            const basePath = this.baseUrl
            // display heartbeat message to show server status
            fetch(basePath)
                    .then(response => this.online = response.ok)

            // create dummy basket list
            fetch(basePath + "/allbaskets")
                    .then(response => {
                        if (response.ok) this.baskets = response.json()
                    })
            this.loading = false
        }
    }
</script>

<style scoped>
    p {
        margin-top: 30px;
        text-align: center;
    }
</style>