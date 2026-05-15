package com.unibo.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unibo.android.data.local.dao.EsameDao
import com.unibo.android.data.local.dao.ObiettivoDao
import com.unibo.android.data.local.entity.EsameEntity
import com.unibo.android.data.local.entity.ObiettivoEntity

@Database(entities = [EsameEntity::class, ObiettivoEntity::class], version = 10)
@TypeConverters(Converters::class)
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
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Inserimento sicuro post-creazione tabelle
                        db.execSQL("""
                            INSERT OR IGNORE INTO obiettivi (id, nome, descrizione, completato, premio_xp)
                            VALUES 
                            (1, 'Primo Passo', 'Registra il tuo primo esame superato', 0, 150),
                            (2, 'Secchione', 'Ottieni la tua prima Lode', 0, 300),
                            (3, 'Maratoneta', 'Supera 3 esami in un mese', 0, 500),
                            (4, 'Giro di Boa', 'Raggiungi 90 CFU', 0, 800)
                        """.trimIndent())
                    }
                })
                .build().also { INSTANCE = it }
            }
        }
    }
}
