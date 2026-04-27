package com.unibo.android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unibo.android.data.local.entity.EsameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EsameDao {
    @Query("SELECT * FROM esami ORDER BY data_esame DESC")
    fun getAllEsami(): Flow<List<EsameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEsame(esame: EsameEntity)

    @Update
    suspend fun updateEsame(esame: EsameEntity)

    @Delete
    suspend fun deleteEsame(esame: EsameEntity)
}
