package org.cryptimeleon.incentive.app.data.database.promotion

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.cryptimeleon.incentive.app.data.database.BigIntegerConverter
import org.cryptimeleon.incentive.app.domain.model.UserUpdateChoice
import org.cryptimeleon.incentive.app.domain.model.module
import java.math.BigInteger

private val json = Json { serializersModule = module }

@TypeConverters(value = [TokenUpdateUserChoiceEntity.UserUpdateChoiceConverter::class, BigIntegerConverter::class])
@Entity(tableName = "token-update-user-choices")
data class TokenUpdateUserChoiceEntity(
    @PrimaryKey
    val promotionId: BigInteger,
    val userUpdateChoice: UserUpdateChoice
) {
    // Converter for storing the choices in the room database
    class UserUpdateChoiceConverter {
        companion object {
            @JvmStatic
            @TypeConverter
            fun fromChoice(userUpdateChoice: UserUpdateChoice): String =
                json.encodeToString(userUpdateChoice)

            @JvmStatic
            @TypeConverter
            fun toChoice(s: String): UserUpdateChoice = json.decodeFromString(s)
        }
    }
}
