import {createRouter, createWebHashHistory} from 'vue-router';

import DsProtMain from '../components/dsprot/DsProtMain.vue'

// Lazy-load routes
const LandingPage = () => import("@/components/LandingPage.vue")
const PrivacyPolicy = () => import("@/components/PrivacyPolicy.vue")

const routes = [
    {path: '/', component: LandingPage},
    {path: '/privacy-policy', component: PrivacyPolicy},
    {path: '/ds-protection', component: DsProtMain}
]

const router = createRouter({
    "history": createWebHashHistory(),
    "routes": routes,
})

export default router