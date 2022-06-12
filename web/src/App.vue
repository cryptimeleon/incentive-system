<template>
  <IncHeader/>
  <div class="max-w-3xl md:mx-auto px-4">
    <LandingPage/>
  </div>
</template>

<script>
import LandingPage from "@/components/LandingPage";
import IncHeader from "@/components/IncHeader"

export default {
  name: 'App',
  components: {
    LandingPage,
    IncHeader
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>
