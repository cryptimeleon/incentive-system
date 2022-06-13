<template>
  <div class="flex flex-col justify-between min-h-screen">
    <IncHeader/>
    <div class="grow max-w-4xl md:mx-auto px-4 self-start">
      <LandingPage/>
    </div>
    <IncFooter/>
  </div>
</template>

<script>
import LandingPage from "@/components/LandingPage";
import IncHeader from "@/components/IncHeader"
import IncFooter from "@/components/IncFooter"

export default {
  name: 'App',
  components: {
    LandingPage,
    IncHeader,
    IncFooter
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
