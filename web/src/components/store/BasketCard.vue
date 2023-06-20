<template>
    <div class="w-full rounded overflow-hidden shadow-md" :class="hasDS ? 'bg-red-200' : 'bg-white'">
        <div class="px-6 py-4 w-full">
            <!-- {{ basket }} -->
            <div class="text-sm mb-2">
                Transaction ID: {{ basket.basketID }}
            </div>
            <div class="text-gray-700 text-base grid md:grid-cols-2 grid-cols-1 gap-4">
                <div class="flex-none md:basis-1/2">
                    <div class="font-bold">
                        🛒 Bought items
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
                <div class="flex-none grow-0 md:basis-1/2">
                    <div>
                        <div class="font-bold">
                            🎁 Reward items
                        </div>
                        <div v-if="basket.rewardItems.length > 0">
                            <div v-for="reward in basket.rewardItems" :key="reward.id">
                                {{ reward.title }}
                            </div>
                        </div>
                        <div v-else>
                            No rewards
                        </div>
                        <div>
                            <div class="font-bold">
                                💾 Customer data
                            </div>
                            <div v-if="hasDS">User has illegally double-spent.</div>
                            <div>
                                {{ hasDS ? basket.dsData.userInfo : "No information"}}
                            </div>
                            <p v-if="hasDS" class="truncate max-w-full">
                                Secret Key: {{ basket.dsData.userSecretExponent.split("INT:")[1] }}
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: "BasketCard",
    props: {
        basket: {}
    },
    computed: {
        hasDS() {
            return Object.hasOwn(this.basket, "dsData")
        }
    }
}
</script>
