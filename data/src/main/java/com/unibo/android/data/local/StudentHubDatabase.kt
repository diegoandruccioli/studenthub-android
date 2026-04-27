package com.unibo.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.unibo.android.data.local.dao.EsameDao
import com.unibo.android.data.local.entity.EsameEntity

@Database(entities = [EsameEntity::class], version = 1)
abstract class StudentHubDatabase : RoomDatabase() {

    abstract fun esameDao(): EsameDao

    companion object {
        @Volatile private var INSTANCE: StudentHubDatabase? = null

        fun getInstance(context: Context): StudentHubDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    StudentHubDatabase::class.java,
                    "studenthub_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
