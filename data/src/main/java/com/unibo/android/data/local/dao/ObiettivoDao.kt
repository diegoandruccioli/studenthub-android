package com.unibo.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unibo.android.data.local.entity.ObiettivoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObiettivoDao {
    @Query("SELECT * FROM obiettivi")
    fun getAllObiettivi(): Flow<List<ObiettivoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObiettivi(obiettivi: List<ObiettivoEntity>)

    @Query("SELECT * FROM obiettivi")
    suspend fun getAllObiettiviSync(): List<ObiettivoEntity>

    @Update
    suspend fun updateObiettivo(obiettivo: ObiettivoEntity)

    @Query("SELECT * FROM obiettivi WHERE id = :id")
    suspend fun getObiettivoById(id: Int): ObiettivoEntity?

    @Query("UPDATE obiettivi SET completato = :completed WHERE id = :id")
    suspend fun updateGoalCompletionStatus(id: Int, completed: Boolean)
}
