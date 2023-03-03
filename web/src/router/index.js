import {createRouter, createWebHashHistory} from 'vue-router';

// Lazy-load routes
const LandingPage = () => import("@/components/LandingPage.vue")
const PrivacyPolicy = () => import("@/components/PrivacyPolicy.vue")
const StoreMain = () => import("@/components/store/StoreMain.vue")
const ProviderRegistrationCoupons = () => import("@/components/ProviderRegistrationCoupons.vue")

const routes = [
    {path: '/', component: LandingPage},
    {path: '/privacy-policy', component: PrivacyPolicy},
    {
        path: '/store-frontend',
        component: StoreMain,
        props: {
            baseUrl: "basket",
            name: "Store One",
            description: "Just a normal store."
        }
    },
    {
        path: '/store-two-frontend',
        component: StoreMain,
        props: {
            baseUrl: "basket-two",
            name: "Store Two",
            description: "This store has some network problems that enable double-spending attacks at this store. " +
                "Attacker's can be identified after some time when the store syncs with the provider."
        }
    },
    {path: '/provider-frontend', component: ProviderRegistrationCoupons}
]

const router = createRouter({
    "history": createWebHashHistory(),
    "routes": routes,
})

export default router