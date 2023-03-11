<template>
  <div class="flex flex-col justify-between min-h-screen bg-gray-50">
    <IncHeader/>
    <div class="grow w-full md:w-[768px] md:mx-auto px-4 self-start">
        <router-view/>
    </div>
    <IncFooter/>
  </div>
</template>

<script>
import IncHeader from "@/components/IncHeader"
import IncFooter from "@/components/IncFooter"

export default {
  name: 'App',
  components: {
    IncHeader,
    IncFooter
  }
}
</script>

Do not delete this!
<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}
</style>
