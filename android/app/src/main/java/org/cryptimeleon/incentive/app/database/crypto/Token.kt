package org.cryptimeleon.incentive.app.database.crypto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class Token(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "serialized_token") val serializedToken: String
)