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

    @Query("SELECT * FROM esami")
    suspend fun getAllEsamiSync(): List<EsameEntity>

    @Query("SELECT * FROM esami WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): EsameEntity?

    @Query("SELECT * FROM esami WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): EsameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEsame(esame: EsameEntity): Long

    @Query("UPDATE esami SET remote_id = :remoteId, pending_sync = 0 WHERE id = :localId")
    suspend fun markSynced(localId: Int, remoteId: Int)

    @Update
    suspend fun updateEsame(esame: EsameEntity)

    @Delete
    suspend fun deleteEsame(esame: EsameEntity)
}
