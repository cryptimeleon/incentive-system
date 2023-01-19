<template>
  <div>
    <div class="text-xl">Provider View - Registration Coupons</div>
    <div v-for="registrationCoupon in registrationCoupons" :key="registrationCoupon.userPublicKey" >
      <p>{{registrationCoupon.userInfo}}</p>
      <p>{{registrationCoupon.userPublicKey}}</p>
      <p>{{registrationCoupon.userSignature}}</p>
      <p>{{registrationCoupon.storePublicKey}}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "ProviderRegistrationCoupons",
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