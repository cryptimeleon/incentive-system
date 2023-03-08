<template>
    <div class="flex flex-col pb-8">
        <div v-for="basket in baskets" :key="basket.basketID" class="w-full rounded overflow-hidden shadow-lg">
            <div class="px-6 py-4">
                <div class="font-bold text-lg mb-2">
                    {{ basket.basketID }}
                </div>
                <div class="text-gray-700 text-base flex md:flex-row flex-col md:space-x-8">
                    <div class="md:min-w-[40%]">
                        <div class="font-bold">
                            Contents
                        </div>
                        <div v-for="item in basket.basketItems" :key="item.name">
                            <div class="flex flex-row space-x-2 justify-between">
                                <div>{{ item.count }}x {{ item.title }}</div>
                                <div>
                                    {{
                                        new Intl.NumberFormat('de-DE', {
                                            style: 'currency',
                                            currency: 'EUR'
                                        }).format((item.price * item.count) / 100)
                                    }}
                                </div>
                            </div>
                        </div>
                        <hr/>
                        <div class="flex flex-row space-x-2 justify-between">
                            <div>
                                Total:
                            </div>
                            <div>
                                {{
                                    new Intl.NumberFormat('de-DE', {
                                        style: 'currency',
                                        currency: 'EUR'
                                    }).format(basket.value / 100)
                                }}
                            </div>
                        </div>
                    </div>
                    <div class="basis-1/2">
                        <div >
                            <div class="font-bold">
                                Rewards
                            </div>
                            <div v-if="basket.rewardItems.length > 0">
                                <div v-for="reward in basket.rewardItems" :key="reward.id">
                                    {{ reward.title }}
                                </div>
                            </div>
                            <div v-else>
                                No rewards
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'BasketList',
    props: {
        baskets: Array
    },
}
</script>