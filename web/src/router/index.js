import {createRouter, createWebHashHistory} from 'vue-router';

// Lazy-load routes
const LandingPage = () => import("@/components/LandingPage.vue")
const PrivacyPolicy = () => import("@/components/PrivacyPolicy.vue")
const ImpressumPage = () => import("@/components/ImpressumPage.vue")
const StoreMain = () => import("@/components/store/StoreMain.vue")
const ProviderRegistrationCoupons = () => import("@/components/ProviderRegistrationCoupons.vue")
const ProductPage = () => import("@/components/ProductPage.vue")

const routes = [
    {
        path: '/',
        component: LandingPage
    },
    {
        path: '/privacy-policy',
        component: PrivacyPolicy
    },
    {
        path: '/impressum',
        component: ImpressumPage
    },
    {
        path: '/store-frontend',
        component: StoreMain,
        props: {
            baseUrl: "store",
            name: "Store One",
            description: "Just a normal store."
        }
    },
    {
        path: '/store-two-frontend',
        component: StoreMain,
        props: {
            baseUrl: "store-two",
            name: "Store Two",
            description: "Another normal store."
        }
    },
    {
        path: '/provider-frontend',
        component: ProviderRegistrationCoupons
    },
    {
        path: '/shopping-items',
        component: ProductPage
    }
]

const router = createRouter({
    "history": createWebHashHistory(),
    "routes": routes,
})

export default router