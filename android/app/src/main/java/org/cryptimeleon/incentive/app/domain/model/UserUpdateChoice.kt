package org.cryptimeleon.incentive.app.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.math.BigInteger
import java.util.*


class SerializableUserChoice {    /*
     * Serializable data classes (to store them in the room database) for the choices a user
     * made for its token update of a promotion.
     */
    sealed interface UserUpdateChoice

    @Serializable
    @SerialName("none")
    object None : UserUpdateChoice

    @Serializable
    @SerialName("earn")
    object Earn : UserUpdateChoice

    @Serializable
    @SerialName("zkp")
    data class ZKP(
        @Serializable(with = UUIDSerializer::class) val tokenUpdateId: UUID
    ) : UserUpdateChoice

}

// Serializer Module that is configured to recover subclasses from above structure of data classes.
val module
    get() = SerializersModule {
        polymorphic(SerializableUserChoice.UserUpdateChoice::class) {
            subclass(SerializableUserChoice.None::class)
            subclass(SerializableUserChoice.Earn::class)
            subclass(SerializableUserChoice.ZKP::class)
        }
    }

/**
 * Data class which combines the promotionId with the corresponding update choice of a user.
 */
data class PromotionUserUpdateChoice(
    val promotionId: BigInteger,
    val userUpdateChoice: SerializableUserChoice.UserUpdateChoice
)

/**
 * Simple kotlin serializer for UUIDs.
 */
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}
