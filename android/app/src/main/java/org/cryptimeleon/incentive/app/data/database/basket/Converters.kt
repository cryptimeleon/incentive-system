package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.TypeConverter
import java.util.*

class Converters {

    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun uuidFromString(string: String): UUID {
        return UUID.fromString(string)
    }


}