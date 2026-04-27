package com.unibo.android.data.local.mapper

import com.unibo.android.data.local.entity.EsameEntity
import com.unibo.android.domain.model.Esame

fun EsameEntity.toDomain(): Esame = Esame(
    id = id,
    nome = nome,
    voto = voto,
    lode = lode,
    cfu = cfu,
    dataEsame = dataEsame
)

fun Esame.toEntity(): EsameEntity = EsameEntity(
    id = id,
    nome = nome,
    voto = voto,
    lode = lode,
    cfu = cfu,
    dataEsame = dataEsame
)
