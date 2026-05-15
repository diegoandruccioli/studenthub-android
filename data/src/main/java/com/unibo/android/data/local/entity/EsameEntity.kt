package com.unibo.android.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import java.time.LocalDate

@Entity(tableName = "esami")
data class EsameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nome") val nome: String,
    @ColumnInfo(name = "voto") val voto: Int,
    @ColumnInfo(name = "lode") val lode: Boolean,
    @ColumnInfo(name = "cfu") val cfu: Int,
    @ColumnInfo(name = "data_esame") val dataEsame: LocalDate,
    @ColumnInfo(name = "remote_id") val remoteId: Int? = null,
    @ColumnInfo(name = "pending_sync") val pendingSync: Boolean = true
)
