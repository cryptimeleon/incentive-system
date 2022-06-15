import { createRouter, createWebHistory} from 'vue-router';

import Home from '../views/Home'
import About from '../views/About'

/*
* View components (from the views-folder) are mapped to routes on the page
* ("what is the root component that I see if I request URL /about ?").
*
* Array of JavaScript objects represents this mapping.
*
* Note that route "/" is mapped to the application's entry point view component
* which is Home.vue.
*/
const routes = [
    {
        path: '/', // route
        name: 'Home',
        component: Home// view component to render (including all its children)
    },
    {
        path: '/about', // route
        name: 'About',
        component: About // view component to render (including all its children)
    }
]

/*
* Create the actual router object
* that leads to URL requests being served as declared in the above mapping.
* To actually use the router, it needs to be registered in the main.js file
* (which is the entry point of the web app).
*/
const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

export default router