package com.unibo.android.ui.screens.libretto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.model.Settings
import com.unibo.android.ui.R
import com.unibo.android.ui.theme.StudentHubTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EsameCard(
    esame: Esame,
    settings: Settings,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    val (badgeBg, badgeFg) = when (settings.temaVoti) {
        "LODE" -> if (esame.lode)
            Color(0xFFFFD54F) to Color(0xFF3E2000)   // amber per 30L
        else
            primaryContainer to onPrimaryContainer
        "RGB" -> when {
            esame.voto >= settings.rgbSogliaAlta ->
                Color(0xFFC8E6C9) to Color(0xFF1B5E20)  // verde
            esame.voto >= settings.rgbSogliaBassa ->
                Color(0xFFFFE0B2) to Color(0xFF4E2600)  // arancione
            else ->
                Color(0xFFFFCDD2) to Color(0xFF7F0000)  // rosso
        }
        else -> primaryContainer to onPrimaryContainer // STANDARD
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = esame.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${esame.cfu} CFU · ${esame.dataEsame.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (esame.lode) "${esame.voto}L" else "${esame.voto}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeFg
                )
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.modifica_esame),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.elimina_esame),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EsameCardPreview() {
    StudentHubTheme {
        EsameCard(
            esame = Esame(
                id = 1,
                nome = "Sistemi Mobili",
                voto = 30,
                lode = true,
                cfu = 6,
                dataEsame = LocalDate.of(2025, 6, 15)
            ),
            settings = Settings("LODE", 18, 30),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
