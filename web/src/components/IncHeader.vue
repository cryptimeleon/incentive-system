<template>
  <div class="">
    <nav class="bg-sky-700 text-slate-100 shadow-lg mb-8 text-sm md:flex md:items-center md:justify-between px-4">
      <div class="flex justify-between">
        <router-link to="/" class="hidden md:block">
          <p class="text-xl font-semibold py-4 text-white">Cryptimeleon Incentive System</p>
        </router-link>
        <router-link to="/" class="md:hidden">
          <p class="text-xl font-semibold py-2 text-white">Incentive System</p>
        </router-link>

        <div @click="showMenu = !showMenu" class="flex md:hidden">
          <button type="button" class="text-white-800">
            <svg viewBox="0 0 24 24" class="w-6 h-6 fill-current">
              <path fill-rule="evenodd"
                d="M4 5h16a1 1 0 0 1 0 2H4a1 1 0 1 1 0-2zm0 6h16a1 1 0 0 1 0 2H4a1 1 0 0 1 0-2zm0 6h16a1 1 0 0 1 0 2H4a1 1 0 0 1 0-2z">
              </path>
            </svg>
          </button>
        </div>
      </div>

      <ul :class="showMenu ? 'flex' : 'hidden'" class="
      font-semibold flex-col py-1 space-y-1
      lg:py-0 lg:flex lg:flex-row lg:space-x-16
      ">
        <li>
          <router-link to="/">
            <span class="text-white">
              Home
            </span>
          </router-link>
        </li>
        <li>
          <router-link to="/ds-protection">
            <span class="text-white">
              Double-Spending Protection
            </span>
          </router-link>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showMenu = ref(false)
</script>

<style scoped>
</style>