import {createRouter, createWebHashHistory} from 'vue-router';

// Lazy-load routes
const LandingPage = () => import("@/components/LandingPage.vue")
const AboutPage = () => import("@/components/AboutPage.vue")
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
        path: '/about',
        component: AboutPage
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
            description: "Just a normal store participating in the incentive system."
        }
    },
    {
        path: '/store-two-frontend',
        component: StoreMain,
        props: {
            baseUrl: "store-two",
            name: "Store Two",
            description: "Another store. Because there is no persistent internet connection between Store One and Store Two, you can spend the same token at both without them immediately noticing (it will, however, be noticed by the provider, who will be able to de-anonymize double-spending users)."
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