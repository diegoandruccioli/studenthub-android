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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unibo.android.domain.model.PuntoAndamento
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.ui.R
import java.util.Locale

@Composable
fun StatisticheScreen(
    viewModel: StatisticheViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.uiState.collectAsStateWithLifecycle()

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
                    color = Color(0xFF1A5A96) // Matching the blueish color from screenshot
                )
            )
            Text(
                text = stringResource(R.string.statistiche_subtitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (stats == null || stats?.cfuSostenuti == 0) {
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
            val s = stats!!
            
            // Cards
            StatCard(
                label = stringResource(R.string.statistiche_media_ponderata),
                value = String.format(Locale.ITALY, "%.1f", s.mediaPonderata),
                icon = Icons.Outlined.School,
                iconColor = Color(0xFF2196F3),
                iconBgColor = Color(0xFFE3F2FD)
            )

            StatCard(
                label = stringResource(R.string.statistiche_cfu_sostenuti),
                value = s.cfuSostenuti.toString(),
                icon = Icons.Outlined.Checklist,
                iconColor = Color(0xFF9C27B0),
                iconBgColor = Color(0xFFF3E5F5)
            )

            StatCard(
                label = stringResource(R.string.statistiche_base_laurea),
                value = String.format(Locale.ITALY, "%.1f", s.baseLaurea),
                icon = Icons.Outlined.BarChart,
                iconColor = Color(0xFF4CAF50),
                iconBgColor = Color(0xFFE8F5E9)
            )

            // Chart Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
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
                            ChartLegendItem(color = Color(0xFF1A5A96), label = stringResource(R.string.statistiche_voti))
                            Spacer(modifier = Modifier.width(12.dp))
                            ChartLegendItem(color = Color(0xFFE53935), label = stringResource(R.string.statistiche_media))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    CareerChart(
                        punti = s.andamentoCarriera,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp)) // Extra space for FAB or BottomNav
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
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
    punti: List<PuntoAndamento>,
    modifier: Modifier = Modifier
) {
    val blueColor = Color(0xFF1A5A96)
    val redColor = Color(0xFFE53935)
    val gridColor = Color(0xFFEEEEEE)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 20.dp.toPx()
        
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Grid lines (voti 18 to 30)
        val stepY = chartHeight / 12
        for (i in 0..12) {
            val y = chartHeight + padding - i * stepY
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx()
            )
            // Y Axis Labels (simplified)
            // drawContext.canvas.nativeCanvas.drawText(...) - omitting for simplicity, or use native canvas
        }

        if (punti.isNotEmpty()) {
            val stepX = if (punti.size > 1) chartWidth / (punti.size - 1) else chartWidth / 2
            
            val pointsVoti = punti.mapIndexed { index, punto ->
                val x = if (punti.size > 1) padding + index * stepX else width / 2
                val y = (chartHeight + padding - (punto.voto - 18) * stepY)
                Offset(x, y)
            }

            val pointsMedia = punti.mapIndexed { index, punto ->
                val x = if (punti.size > 1) padding + index * stepX else width / 2
                val y = (chartHeight + padding - (punto.mediaPonderataProgressiva - 18) * stepY).toFloat()
                Offset(x, y)
            }

            // Draw Media Line
            if (pointsMedia.size > 1) {
                val pathMedia = Path().apply {
                    moveTo(pointsMedia[0].x, pointsMedia[0].y)
                    for (i in 1 until pointsMedia.size) {
                        lineTo(pointsMedia[i].x, pointsMedia[i].y)
                    }
                }
                drawPath(
                    path = pathMedia,
                    color = redColor,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Draw Voti Points
            pointsVoti.forEach { point ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = blueColor,
                    radius = 5.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            
            // Draw last Media Point as solid dot
            if (pointsMedia.isNotEmpty()) {
                drawCircle(
                    color = redColor,
                    radius = 4.dp.toPx(),
                    center = pointsMedia.last()
                )
            }
        }
    }
}
