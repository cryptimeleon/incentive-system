package org.cryptimeleon.incentive.app.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.math.BigInteger

class BigIntegerConverter {
    companion object {
        @JvmStatic
        @TypeConverter
        fun fromBigInteger(b: BigInteger): String = Gson().toJson(b)

        @JvmStatic
        @TypeConverter
        fun toBigInteger(s: String): BigInteger = Gson().fromJson(s, BigInteger::class.java)
    }
}
