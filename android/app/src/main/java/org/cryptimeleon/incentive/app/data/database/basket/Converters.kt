package org.cryptimeleon.incentive.app.data.database.basket

import androidx.room.TypeConverter
import org.cryptimeleon.incentive.app.data.database.basket.BasketEntity
import org.cryptimeleon.incentive.app.data.database.basket.BasketItemEntity
import org.cryptimeleon.incentive.app.domain.model.Basket
import org.cryptimeleon.incentive.app.domain.model.BasketItem
import java.util.*

class Converters{

    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun uuidFromString(string: String): UUID {
        return UUID.fromString(string)
    }


}