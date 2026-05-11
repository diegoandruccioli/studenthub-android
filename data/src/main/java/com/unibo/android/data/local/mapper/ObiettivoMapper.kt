package com.unibo.android.data.local.mapper

import com.unibo.android.data.local.entity.ObiettivoEntity
import com.unibo.android.domain.model.Obiettivo

fun ObiettivoEntity.toDomain() = Obiettivo(
    id = id,
    nome = nome,
    descrizione = descrizione,
    completato = completato,
    premioXp = premioXp
)

fun Obiettivo.toEntity() = ObiettivoEntity(
    id = id,
    nome = nome,
    descrizione = descrizione,
    completato = completato,
    premioXp = premioXp
)
