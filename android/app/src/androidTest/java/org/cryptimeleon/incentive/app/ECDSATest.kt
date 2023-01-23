package org.cryptimeleon.incentive.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cryptimeleon.craco.common.ByteArrayImplementation
import org.cryptimeleon.craco.sig.ecdsa.ECDSASignatureScheme
import org.cryptimeleon.craco.sig.ecdsa.ECDSASigningKey
import org.cryptimeleon.craco.sig.ecdsa.ECDSAVerificationKey
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ECDSATest {

    @Test
    fun testECDSA() {
        val ecdsaSignatureScheme = ECDSASignatureScheme()
        val keypair = ecdsaSignatureScheme.generateKeyPair()

        val signature = ecdsaSignatureScheme.sign(ByteArrayImplementation("Test".encodeToByteArray()), keypair.signingKey)
        assert(ecdsaSignatureScheme.verify(ByteArrayImplementation("Test".encodeToByteArray()), signature, keypair.verificationKey))
    }

    @Test
    fun testECDSASerialization() {
        val ecdsaSignatureScheme = ECDSASignatureScheme()
        val keypair = ecdsaSignatureScheme.generateKeyPair()
        val sk = ECDSASigningKey(keypair.signingKey.representation)
        val vk = ECDSAVerificationKey(keypair.verificationKey.representation)

        Assert.assertEquals(vk, keypair.verificationKey)
        Assert.assertEquals(sk, keypair.signingKey)
    }
}
