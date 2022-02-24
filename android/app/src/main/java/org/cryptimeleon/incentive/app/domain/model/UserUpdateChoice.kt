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

data class PromotionUserUpdateChoice(
    val promotionId: BigInteger,
    val userUpdateChoice: UserUpdateChoice
)

val module = SerializersModule {
    polymorphic(UserUpdateChoice::class) {
        subclass(None::class)
        subclass(Earn::class)
        subclass(ZKP::class)
    }
}

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
