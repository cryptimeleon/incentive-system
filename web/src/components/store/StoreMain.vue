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
            baskets: [],
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
                    .then(data => this.baskets = data)
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