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
import {toArray} from "uri-js/dist/esnext/util";

export default {
  name: "ProviderRegistrationCoupons",
  methods: {toArray},
  data() {
    return {
      registrationCoupons: [
        {
          "userInfo": "Paul",
          "userPublicKey": "{\"__type\":\"OBJ\",\"upk\":[\"INT:1\",\"INT:10650384e65637a687a3275ca12b687f02f30779b85eb83ff8bd305803ab32a\",\"INT:1b510b46309b18fcc52afa35172635f90693400a48feff9e82c9147cab6d1911\"]}",
          "signature": "\"BYTES:MEYCIQCFMBzjzpkqWkqb7B2cZ84yDZvt+t297cmeFA5joq5swwIhAL5p40+B3YGYM01UPA06zsMS39I0beyLN39cF0MmREpY\"",
          "storePublicKey": "{\"__type\":\"OBJ\",\"ecdsaVerificationKey\":{\"__type\":\"REPR\",\"class\":\"org.cryptimeleon.craco.sig.ecdsa.ECDSAVerificationKey\",\"repr\":\"STRING:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAErmHZzoN2G0YDdncQzBjIF9LgkLGKgzu6ftyYau4TQubiOFz2VJ2kzDyQuP3FXvE1rGIFDoNaVM\\/mhyLIdZ0oJA==\"}}"
        },
        {
          "userInfo": "Paul",
          "userPublicKey": "{\"__type\":\"OBJ\",\"upk\":[\"INT:1\",\"INT:10650384e65637a687a3275ca12b687f02f30779b85eb83ff8bd305803ab32a\",\"INT:1b510b46309b18fcc52afa35172635f90693400a48feff9e82c9147cab6d1911\"]}1",
          "signature": "\"BYTES:MEYCIQCFMBzjzpkqWkqb7B2cZ84yDZvt+t297cmeFA5joq5swwIhAL5p40+B3YGYM01UPA06zsMS39I0beyLN39cF0MmREpY\"",
          "storePublicKey": "{\"__type\":\"OBJ\",\"ecdsaVerificationKey\":{\"__type\":\"REPR\",\"class\":\"org.cryptimeleon.craco.sig.ecdsa.ECDSAVerificationKey\",\"repr\":\"STRING:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAErmHZzoN2G0YDdncQzBjIF9LgkLGKgzu6ftyYau4TQubiOFz2VJ2kzDyQuP3FXvE1rGIFDoNaVM\\/mhyLIdZ0oJA==\"}}"
        },
        {
          "userInfo": "Paul",
          "userPublicKey": "{\"__type\":\"OBJ\",\"upk\":[\"INT:1\",\"INT:10650384e65637a687a3275ca12b687f02f30779b85eb83ff8bd305803ab32a\",\"INT:1b510b46309b18fcc52afa35172635f90693400a48feff9e82c9147cab6d1911\"]}2",
          "signature": "\"BYTES:MEYCIQCFMBzjzpkqWkqb7B2cZ84yDZvt+t297cmeFA5joq5swwIhAL5p40+B3YGYM01UPA06zsMS39I0beyLN39cF0MmREpY\"",
          "storePublicKey": "{\"__type\":\"OBJ\",\"ecdsaVerificationKey\":{\"__type\":\"REPR\",\"class\":\"org.cryptimeleon.craco.sig.ecdsa.ECDSAVerificationKey\",\"repr\":\"STRING:MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAErmHZzoN2G0YDdncQzBjIF9LgkLGKgzu6ftyYau4TQubiOFz2VJ2kzDyQuP3FXvE1rGIFDoNaVM\\/mhyLIdZ0oJA==\"}}"
        }
      ]
    };
  },
  async created() {
    fetch("/incentive/registration-coupons")
        .then(response => response.json())
        //.then(data => (this.registrationCoupons = data));
  }
}
</script>

<style scoped>

</style>