<template>
    <!-- CSS class depends on whether transaction is valid or not.
    Property 'transaction.valid' is used to determine this. -->
    <div :class="[transaction.valid ? '' : 'invalid', 'transaction']">
        <h3>ID: {{transaction.tid}}</h3>
        <p>Chosen reward: {{transaction.userChoice}}</p>
        <p>Promotion ID: {{transaction.pid}}</p>
    </div>
</template>

<script>
    export default {
        name: 'TransactionUI',
        props: {
            transaction: Object
        }
    }
</script>

<style scoped>
    .transaction h3 {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .transaction p {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .transaction {
        background: #f4f4f4;
        margin: 5px;
        padding: 10px 20px;
        border-left: 15px solid green;
    }

    .transaction.invalid {
        border-left: 15px solid red;
    }
</style>