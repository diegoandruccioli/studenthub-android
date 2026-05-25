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
    /** Esami visibili in UI: esclude quelli con delete remoto pendente */
    @Query("SELECT * FROM esami WHERE pending_delete = 0 ORDER BY data_esame DESC")
    fun getAllEsami(): Flow<List<EsameEntity>>

    @Query("SELECT * FROM esami")
    suspend fun getAllEsamiSync(): List<EsameEntity>

    @Query("SELECT * FROM esami WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): EsameEntity?

    @Query("SELECT * FROM esami WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): EsameEntity?

    @Query("SELECT * FROM esami WHERE pending_sync = 1 AND pending_delete = 0")
    suspend fun getUnsyncedEsami(): List<EsameEntity>

    /** Esami da eliminare sul server (marcati pending_delete con remoteId noto) */
    @Query("SELECT * FROM esami WHERE pending_delete = 1 AND remote_id IS NOT NULL")
    suspend fun getPendingDeleteEsami(): List<EsameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEsame(esame: EsameEntity): Long

    @Query("UPDATE esami SET remote_id = :remoteId, pending_sync = 0 WHERE id = :localId")
    suspend fun markSynced(localId: Int, remoteId: Int)

    @Query("UPDATE esami SET pending_delete = 1 WHERE id = :id")
    suspend fun markPendingDelete(id: Int)

    @Update
    suspend fun updateEsame(esame: EsameEntity)

    @Delete
    suspend fun deleteEsame(esame: EsameEntity)

    @Query("SELECT SUM(voto * cfu + (CASE WHEN lode = 1 THEN 50 ELSE 0 END)) FROM esami WHERE pending_delete = 0")
    fun getTotalXp(): Flow<Int?>
}
