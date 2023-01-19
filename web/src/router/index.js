import {createRouter, createWebHashHistory} from 'vue-router';

// Lazy-load routes
const LandingPage = () => import("@/components/LandingPage.vue")
const PrivacyPolicy = () => import("@/components/PrivacyPolicy.vue")
const DsProtMain = () => import("@/components/dsprot/DsProtMain.vue")
const StoreMain = () => import("@/components/store/StoreMain.vue")
const ProviderRegistrationTokens = () => import("@/components/ProviderRegistrationTokens.vue")

const routes = [
    {path: '/', component: LandingPage},
    {path: '/privacy-policy', component: PrivacyPolicy},
    {path: '/ds-protection', component: DsProtMain},
    {path: '/store-frontend', component: StoreMain},
    {path: '/provider-registration-tokens', component: ProviderRegistrationTokens}
]

const router = createRouter({
    "history": createWebHashHistory(),
    "routes": routes,
})

export default router