package org.cryptimeleon.incentive.app.data.database.promotion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotions")
    fun observePromotions(): Flow<List<PromotionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPromotions(promotionEntities: List<PromotionEntity>)

    @Query("DELETE FROM promotions")
    fun deletePromotions()

    @Query("SELECT * FROM `token-update-user-choices`")
    fun observerUserTokenUpdateChoices(): Flow<List<TokenUpdateUserChoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putUserTokenUpdateChoice(tokenUpdateUserChoiceEntity: TokenUpdateUserChoiceEntity)
}
