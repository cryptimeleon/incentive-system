<template>
    <!-- Double curly-braces used to references properties ("props") of the component to generate dynamic HTML. -->
    <h1>{{title}}</h1>
</template>

<script>
    export default {
        name: 'Header',

        /*
        * Properties of the component,
        * can be set via HTML attribute syntax when the component is instantiated.
        */
        props: {
            title: String
        },

        /*
        * Components that this component contains.
        * Later: buttons to filter transactions.
        */
        components: {

        }
    }
</script>

<style scoped> /* scoped: this styling only applies to this component, not beyond */
    header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
    }
</style>