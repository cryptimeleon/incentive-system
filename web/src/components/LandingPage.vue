<template>
    <div class="prose prose-slate">
        <h1>The Incentimeleon system</h1>
        <p>
            <strong>Hey!</strong> This site is part of our prototype implementation of a privacy-preserving incentive system.
            We use <strong>modern cryptographic methods</strong> (zero-knowledge proofs, pairing-based signatures, etc.) to reclaim <strong>user privacy</strong> when taking part in loyalty promotions.
        </p>
        <p>
            This website, together with our app, allows you to explore and try out our solution.
        </p>

        <h1>
            What's next?
        </h1>
        <ul> <!-- Ideally, this becomes a much more prominent collection of huge buttons to click -->
            <li>🧑‍🏫 Learn: how does Incentimeleon work? [<router-link to="/about">📚 About page</router-link>]</li>
            <li>🛒 Experience: what does the user see? [<a>📱 Play Store</a> / <a>📽️ Video</a>]</li>
            <li>🧑‍💻 Check: what does the provider see? [<router-link to="/store-frontend">🏪 Store view</router-link> / <router-link to="provider-frontend">🌍 Provider view</router-link>]</li>
        </ul>
    </div>
</template>

<script setup>
</script>

<style scoped>

</style>