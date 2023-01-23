<template>
  <div class="w-full" >
    <div class="text-3xl font-bold">Provider View - Registration Coupons</div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 py-4">
      <div v-for="registrationCoupon in registrationCoupons" :key="registrationCoupon.userPublicKey" >
        <div class="max-w-sm rounded overflow-hidden shadow-lg">
          <div class="px-6 py-4">
            <div class="font-bold text-xl mb-2">
              User: {{registrationCoupon.userInfo}}
            </div>
            <div class="text-gray-700 text-base">
              <p class="font-bold">UserPublicKey</p>
              <p class="break-all">({{registrationCoupon.userPublicKey.split("INT").slice(2,4).map(s => s.replace(/\W/g, '')).join(", ")}})</p>
              <p class="font-bold">Signature</p>
              <p class="break-all">{{registrationCoupon.signature.replace("BYTES:", '').replaceAll('"', '')}}</p>
              <p class="font-bold">Store Public Key</p>
              <p class="break-all">{{registrationCoupon.storePublicKey.split("STRING:")[1].replace('"\}\}', '')}}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: "ProviderRegistrationCoupons",
  methods: {},
  data() {
    return {
      registrationCoupons: []
    };
  },
  async created() {
    fetch("/incentive/registration-coupons")
        .then(response => response.json())
        .then(data => (this.registrationCoupons = data));
  }
}
</script>

<style scoped>

</style>