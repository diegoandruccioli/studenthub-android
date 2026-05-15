package com.unibo.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unibo.android.data.local.dao.EsameDao
import com.unibo.android.data.local.dao.ObiettivoDao
import com.unibo.android.data.local.entity.EsameEntity
import com.unibo.android.data.local.entity.ObiettivoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [EsameEntity::class, ObiettivoEntity::class], version = 3)
abstract class StudentHubDatabase : RoomDatabase() {

    abstract fun esameDao(): EsameDao
    abstract fun obiettiviDao(): ObiettivoDao

    companion object {
        @Volatile private var INSTANCE: StudentHubDatabase? = null

        fun getInstance(context: Context): StudentHubDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    StudentHubDatabase::class.java,
                    "studenthub_db"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        prepopulate(context)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        prepopulate(context)
                    }

                    private fun prepopulate(context: Context) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).obiettiviDao()
                            if (dao.getAllObiettiviSync().isEmpty()) {
                                dao.insertObiettivi(
                                    listOf(
                                        ObiettivoEntity(1, "Primo Passo", "Registra il tuo primo esame superato", false, 150),
                                        ObiettivoEntity(2, "Secchione", "Ottieni la tua prima Lode", false, 300),
                                        ObiettivoEntity(3, "Maratoneta", "Supera 3 esami in un mese", false, 500),
                                        ObiettivoEntity(4, "Giro di Boa", "Raggiungi 90 CFU", false, 800)
                                    )
                                )
                            }
                        }
                    }
                })
                .build().also { INSTANCE = it }
            }
        }
    }
}
