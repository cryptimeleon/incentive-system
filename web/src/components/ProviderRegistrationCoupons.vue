<template>
  <div class="w-full">
    <div class="flex flex-row space-x-4">
      <div class="text-3xl font-bold">Provider</div>
      <ServiceStatus :loading="loading" :online="online"/>
    </div>
    <div class="prose text-lg pb-2">
      Some provider text here.
    </div>
    <div class="text-2xl font-bold">Registration Coupons</div>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 py-4">
      <div v-for="registrationCoupon in registrationCoupons" :key="registrationCoupon.userPublicKey">
        <div class="max-w-sm rounded overflow-hidden shadow-md bg-white">
          <div class="px-6 py-4">
            <div class="font-bold text-xl mb-2">
              User: {{ registrationCoupon.userInfo }}
            </div>
            <div class="text-gray-700 text-base">
              <p class="font-bold">UserPublicKey</p>
              <p class="break-all">
                ({{
                  registrationCoupon.userPublicKey.split("INT").slice(2, 4).map(s => s.replace(/\W/g, '')).join(", ")
                }})</p>
              <p class="font-bold">Signature</p>
              <p class="break-all">
                {{ registrationCoupon.signature.replace("BYTES:", '').replaceAll('"', '') }}</p>
              <p class="font-bold">Store Public Key</p>
              <p class="break-all">
                {{ registrationCoupon.storePublicKey.split("STRING:")[1].replace('"\}\}', '') }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import ServiceStatus from "@/components/ServiceStatus.vue";

export default {
  name: "ProviderRegistrationCoupons",
  components: {ServiceStatus},
  methods: {},
  data() {
    return {
      registrationCoupons: [],
      online: false,
      loading: true
    };
  },
  async created() {
    fetch("/provider").then((response) => {
      this.online = response.ok
      this.loading = false
    })

    fetch("/provider/registration-coupons")
        .then(response => {
          if (!response.ok) throw Error(response.statusText)
          return response
        })
        .then(response => response.json())
        .then(data => this.registrationCoupons = data)
        .catch(error => console.error(error))
  }
}
</script>

<style scoped>

</style>