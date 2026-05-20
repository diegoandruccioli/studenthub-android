package com.unibo.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.unibo.android.data.local.entity.LeaderboardEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the global leaderboard.
 */
@Dao
interface LeaderboardDao {
    /**
     * Observes the leaderboard entries sorted by total XP.
     */
    @Query("SELECT * FROM leaderboard ORDER BY xpTotali DESC")
    fun getLeaderboard(): Flow<List<LeaderboardEntity>>

    /**
     * Inserts a list of leaderboard entries, replacing any existing ones.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<LeaderboardEntity>)

    /**
     * Clears all leaderboard entries.
     */
    @Query("DELETE FROM leaderboard")
    suspend fun clearAll()

    /**
     * Atomically clears the local leaderboard and inserts fresh data from the network.
     */
    @Transaction
    suspend fun refreshLeaderboard(entries: List<LeaderboardEntity>) {
        clearAll()
        insertAll(entries)
    }
}
