<template>
    <div class="prose prose-slate">
        <h1>About Incentimeleon</h1>
        <p>On this page, we explain how Incentimleon works and how it differs from typical implementations of incentive systems</p>

        <h2>What is an incentive system and how does it usually work?</h2>
        <p>
            An incentive system (or customer loyalty system) is designed to reward customers for certain behavior. Usually, customers collect "points" for their purchases, and can later spend those points on rewards.
            Typical examples are loyalty point systems in supermarkets or airline miles.
        </p>
        <video style="width:100%;
        border:0;
        -webkit-mask-image: -webkit-radial-gradient(white, black);
        -webkit-backface-visibility: hidden;
        -moz-backface-visibility: hidden;" 
        autoplay muted loop playsinline>
            <source src="assets/is-general.mp4" type="video/mp4">
            Your browser does not support the video tag.
        </video>
        <p>
            Incentive systems in practice are very simple: to issue/redeem points, stores have an active database connection, customers present a card containing their database entry ID, which allows the incentive system provider to update the correct database entry.
        </p>
        <p>
            Those systems come at a huge <strong>cost of privacy</strong>: while grocery shopping used to be a fully anonymous activity, presenting a unique ID enables the store/provider to associate otherwise harmless data ("<i>someone</i> just bought a teddy bear and tomatoes") to the user's identity ("<i>Jimothy</i> just bought a teddy bear and tomatoes; the same <i>Jimothy</i> who bought a pregnancy test yesterday").
        </p>

        <h2>How does Incentimeleon work?</h2>
        Instead of storing the user's data (e.g., their point count) in a central database, <strong>Incentimeleon stores user data on the user's phone</strong> in the form of an authenticated token. 
        When the data needs to be updated (e.g., adding 7 points to the current point count), the provider <i>blindly</i> executes this update, without learning the user's identity or the old or the updated data.
        <video style="width:100%;
        border:0;
        -webkit-mask-image: -webkit-radial-gradient(white, black);
        -webkit-backface-visibility: hidden;
        -moz-backface-visibility: hidden;" 
        autoplay muted loop playsinline>
            <source src="assets/is-additive-update.mp4" type="video/mp4">
            Your browser does not support the video tag.
        </video>
        This means that the provider or store never actually get to see the point counts or the user ID of a shopper, preventing the creation of user profiles in a strong cryptographic sense. 


        <h2>What are the challenges with this approach?</h2>
        <p>
            One of the main challenges is double-spending: When a user spends 700 points, how do we invalidate the user's old 1000 point token and force him to use his new 300 point token?
        </p>
        For this, Incentimeleon supports two mechanisms (in tandem):
        <ul>
            <li><strong>Online double-spending prevention</strong>: can detect double-spending before it happens, but requires persistent database connection.</li>
            <li><strong>Offline double-spending detection</strong>: stores can speculatively accept tokens even when offline. In double-spending is detected later (e.g., when online again), the identity of the malicious user is revealed and losses can be retroactively recouped (e.g., through the legal system).</li>
        </ul>


        <h2>Why would anyone deploy Incentimeleon? Isn't collecting data the whole point?</h2>
        <p>
            Indeed, many digital incentive systems in practice are mainly motivated by data collection. 
            However, there are actually many incentive systems in operation that do not collect data at all. We all know that smaller stores often have analog stamp cards (buy 10 sandwiches, get one free), which simply reward loyalty, but clearly do not collect any private data. 
            Incentimeleon combines the privacy of the analog paper-based solution with the convenience and expressiveness of digital systems. It supports many different ways of rewarding customers apart from simple point collection (enabling gamification).
        </p>

        <p>
            Since Incentimeleon demonstrates that a privacy-preserving solution for digital incentive systems is feasible, users and law makers can start demanding such a solution. 
        </p>

        <h2>What's the bigger picture here?</h2>
        <p>
            There many other applications that can be phrased as "collect points and spend them". 
            For example, bus tickets can be bought in bulk ("collect 10 tickets") and then spent anonymously on the bus. Using Incentimeleon's techniques, one could even implement automatic fare capping in a way that does not require the bus provider to learn your movement patterns. 
        </p>
        <p>
            Furthermore, the more general idea of storing user data <i>with the user</i> and doing privacy-preserving updates on that data is a powerful idea to enable privacy in <i>many</i> contexts. Further cryptographic design work may be needed depending on the concrete application. Contact us with your idea!
        </p>
        <!-- Can replace database with data stored at user, can do updates. -->
        <!-- Similar apps: buy bus tickets with prepaid card w/o revealing anything. -->

        <h2>How was Incentimeleon developed?</h2>
        Cryptimeleon is an <a href="https://github.com/cryptimeleon/incentive-system">open-source prototype</a> developed at Paderborn University as part of the SFB901 "<a href="https://sfb901.uni-paderborn.de/projects/project-area-t/subproject-t2">On-The-Fly Computing</a>".
        If is largely based on the paper <a href="https://eprint.iacr.org/2020/382">Privacy-Preserving Incentive Systems with Highly Efficient Point-Collection</a>. 
        It has been developed using the <a href="https://cryptimeleon.org/">Cryptimeleon</a> cryptographic prototyping library.
    </div>
</template>

<script setup>
</script>

<style scoped>

</style>