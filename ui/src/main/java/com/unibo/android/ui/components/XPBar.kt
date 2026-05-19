package com.unibo.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unibo.android.domain.utils.GamificationUtils

@Composable
fun XPBar(
    xp: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    val levelTitle = GamificationUtils.getLevelTitle(level)
    val xpInLevel = GamificationUtils.getXpInCurrentLevel(xp)
    val progress = xpInLevel.toFloat() / GamificationUtils.XP_PER_LEVEL.toFloat()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lv. $level - $levelTitle",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Contenitore Barra
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                .background(Color.LightGray, RoundedCornerShape(4.dp))
        ) {
            // Progresso
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(24.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    .background(Color(0xFF4A80B4)) // Colore blu dal mockup
                    .border(1.dp, Color.Black, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "$xpInLevel / ${GamificationUtils.XP_PER_LEVEL - 1} XP",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
