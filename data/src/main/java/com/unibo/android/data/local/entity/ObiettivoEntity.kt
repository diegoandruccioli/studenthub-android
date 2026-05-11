package com.unibo.android.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "obiettivi")
data class ObiettivoEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "nome") val nome: String,
    @ColumnInfo(name = "descrizione") val descrizione: String,
    @ColumnInfo(name = "completato") val completato: Boolean,
    @ColumnInfo(name = "premio_xp") val premioXp: Int
)
