package com.unibo.android.ui.screens.statistiche

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unibo.android.ui.R

@Composable
fun StatisticheScreen(
    viewModel: StatisticheViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is StatisticheUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is StatisticheUiState.Empty -> {
            StatisticheContent(
                uiModel = null,
                modifier = modifier
            )
        }
        is StatisticheUiState.Success -> {
            StatisticheContent(
                uiModel = state.uiModel,
                modifier = modifier
            )
        }
        is StatisticheUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StatisticheContent(
    uiModel: StatisticheUiModel?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Breadcrumb and Titles
        Column {
            Text(
                text = stringResource(R.string.statistiche_breadcrumb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.statistiche_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = stringResource(R.string.statistiche_subtitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiModel == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.statistiche_vuoto),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Cards (using pre-formatted strings from UI Model)
            StatCard(
                label = stringResource(R.string.statistiche_media_ponderata),
                value = uiModel.mediaPonderata,
                icon = Icons.Outlined.School,
                iconColor = MaterialTheme.colorScheme.primary,
                iconBgColor = MaterialTheme.colorScheme.primaryContainer
            )

            StatCard(
                label = stringResource(R.string.statistiche_cfu_sostenuti),
                value = uiModel.cfuSostenuti,
                icon = Icons.Outlined.Checklist,
                iconColor = MaterialTheme.colorScheme.tertiary,
                iconBgColor = MaterialTheme.colorScheme.tertiaryContainer
            )

            StatCard(
                label = stringResource(R.string.statistiche_base_laurea),
                value = uiModel.baseLaurea,
                icon = Icons.Outlined.BarChart,
                iconColor = MaterialTheme.colorScheme.secondary,
                iconBgColor = MaterialTheme.colorScheme.secondaryContainer
            )

            // Chart Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.statistiche_andamento_carriera),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChartLegendItem(
                                color = MaterialTheme.colorScheme.primary, 
                                label = stringResource(R.string.statistiche_voti)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            ChartLegendItem(
                                color = MaterialTheme.colorScheme.error, 
                                label = stringResource(R.string.statistiche_media)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Truly "Dumb" Career Chart
                    CareerChart(
                        puntiVotiRelativi = uiModel.puntiVoti,
                        puntiMediaRelativi = uiModel.puntiMedia,
                        yMediaFissaRelativa = uiModel.yMediaFissa,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun CareerChart(
    puntiVotiRelativi: List<OffsetRelativo>,
    puntiMediaRelativi: List<OffsetRelativo>,
    yMediaFissaRelativa: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Animazione per l'ingresso fluido delle linee e dei punti
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(puntiVotiRelativi) {
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1500))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val progress = animationProgress.value

        // ARCHITETTURA: Margini di sicurezza per evitare l'Edge Clipping
        val margin = 12.dp.toPx()
        val chartWidth = width - (margin * 2)
        val chartHeight = height - (margin * 2)

        // 1. SINGLE SOURCE OF TRUTH: Mappatura Coordinate Pixel
        // Mappiamo entrambi i set di dati una sola volta per garantire coerenza topologica
        val pointsMedia = puntiMediaRelativi.map { rel ->
            Offset(
                x = margin + (rel.x * chartWidth),
                y = (height - margin) - (rel.y * chartHeight)
            )
        }
        val pointsVoti = puntiVotiRelativi.map { rel ->
            Offset(
                x = margin + (rel.x * chartWidth),
                y = (height - margin) - (rel.y * chartHeight)
            )
        }

        // 2. GRID (Background Layer - Livello più basso)
        val numGridLines = 5
        for (i in 0..numGridLines) {
            val y = (height - margin) - (i * (chartHeight / numGridLines))
            drawLine(
                color = outlineVariantColor.copy(alpha = 0.2f),
                start = Offset(margin, y),
                end = Offset(width - margin, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 3. ANDAMENTO MEDIA (Middle Layer - Path + Gradiente)
        // Usiamo errorColor (Rosso) per coerenza con la legenda "Media"
        if (pointsMedia.isNotEmpty()) {
            val pathMedia = Path().apply {
                moveTo(pointsMedia[0].x, pointsMedia[0].y)
                for (i in 1 until pointsMedia.size) {
                    val prev = pointsMedia[i - 1]
                    val curr = pointsMedia[i]
                    val cp1X = prev.x + (curr.x - prev.x) / 2f
                    val cp2X = prev.x + (curr.x - prev.x) / 2f
                    cubicTo(cp1X, prev.y, cp2X, curr.y, curr.x, curr.y)
                }
            }

            // Area riempita con gradiente (Sfondo dell'andamento)
            val fillPath = Path().apply {
                addPath(pathMedia)
                lineTo(pointsMedia.last().x, height - margin)
                lineTo(pointsMedia.first().x, height - margin)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(errorColor.copy(alpha = 0.2f * progress), Color.Transparent),
                    startY = pointsMedia.minOf { it.y },
                    endY = height - margin
                )
            )

            // Linea della media continua e smussata
            drawPath(
                path = pathMedia,
                color = errorColor.copy(alpha = progress),
                style = Stroke(
                    width = 3.dp.toPx(),
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )
        }

        // 4. NODI DEI VOTI (Livello Superiore - Layer Voti)
        // Usiamo primaryColor (Blu) per coerenza con la legenda "Voti"
        pointsVoti.forEachIndexed { index, point ->
            if (index < pointsVoti.size * progress) {
                // Effetto alone
                drawCircle(
                    color = primaryColor.copy(alpha = 0.2f),
                    radius = 7.dp.toPx(),
                    center = point
                )
                // Bordo punto
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
                // Cuore bianco per contrasto
                drawCircle(
                    color = surfaceColor,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }

        // 5. LINEA TARGET / MEDIA FISSA (Foreground Layer - Z-Order Finale)
        // Disegnata per ultima per sovrastare gradiente e andamento
        val yTarget = (height - margin) - (yMediaFissaRelativa * chartHeight)
        drawLine(
            color = errorColor.copy(alpha = 0.7f * progress),
            start = Offset(margin, yTarget),
            end = Offset(margin + (chartWidth * progress), yTarget),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
        )
        
        // Indicatore luminoso finale sulla media fissa
        if (progress > 0.9f) {
            drawCircle(
                color = errorColor.copy(alpha = (progress - 0.9f) * 10),
                radius = 5.dp.toPx(),
                center = Offset(margin + chartWidth, yTarget)
            )
        }
    }
}
