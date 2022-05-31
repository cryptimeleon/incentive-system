<template>
  <div class="container">
    <Header title="Cryptimeleon Incentive System Double-spending Protection Service" />

    <!-- Spits out the view component that the router mapping prescribes
    for the requested URL. -->
    <router-view></router-view>

    <Footer />
    </div>
</template>

<script>
  import Header from "./components/Header.vue";
  import Footer from "./components/Footer.vue";

  export default {
    name: 'App',
    components: {
      Header,
      Footer
    }
  }
</script>

<style>
  #app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 60px;
  }

  .container {
  max-width: 1500px;
  margin: 30px auto;
  overflow: auto;
  min-height: 300px;
  border: 1px solid steelblue;
  padding: 30px;
  border-radius: 5px;
}
</style>
