<template>
  <div>
    <div v-for="registrationCoupon in registrationCoupons">
      <p>{{registrationCoupon.userInfo}}</p>
      <p>{{registrationCoupon.userPublicKey}}</p>
      <p>{{registrationCoupon.userSignature}}</p>
      <p>{{registrationCoupon.storePublicKey}}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "ProviderRegistrationTokens",
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