package com.unibo.android.ui.screens.libretto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unibo.android.domain.model.Esame
import com.unibo.android.ui.R
import com.unibo.android.ui.screens.gamification.GamificationUiState
import com.unibo.android.ui.screens.gamification.GamificationViewModel
import com.unibo.android.ui.screens.gamification.components.XpBar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun LibrettoScreen(
    viewModel: LibrettoViewModel,
    gamificationViewModel: GamificationViewModel,
    modifier: Modifier = Modifier
) {
    val esami by viewModel.esami.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val gamificationState by gamificationViewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var esameToEdit by remember { mutableStateOf<Esame?>(null) }
    var esameToDelete by remember { mutableStateOf<Esame?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sezione Gamification (Barra XP)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                when (val state = gamificationState) {
                    is GamificationUiState.Loading -> {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                    is GamificationUiState.Success -> {
                        XpBar(
                            level = state.stats.level,
                            levelTitle = state.stats.levelTitle,
                            xpLabel = state.stats.xpLabel,
                            progressPercentage = state.stats.progressPercentage
                        )
                    }
                    is GamificationUiState.Error -> {
                        // In caso di errore non mostriamo la barra, o potremmo mostrare un placeholder
                    }
                }
            }

            // Filtri sort
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.libretto_ordina_per),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterChip(
                    selected = sortBy == SortBy.DATA,
                    onClick = { viewModel.setSortBy(SortBy.DATA) },
                    label = { Text(stringResource(R.string.sort_data)) }
                )
                FilterChip(
                    selected = sortBy == SortBy.VOTO,
                    onClick = { viewModel.setSortBy(SortBy.VOTO) },
                    label = { Text(stringResource(R.string.sort_voto)) }
                )
                FilterChip(
                    selected = sortBy == SortBy.CFU,
                    onClick = { viewModel.setSortBy(SortBy.CFU) },
                    label = { Text(stringResource(R.string.sort_cfu)) }
                )
                TextButton(onClick = { viewModel.toggleSortOrder() }) {
                    Text(
                        text = if (sortOrder == SortOrder.DESC)
                            stringResource(R.string.order_desc)
                        else
                            stringResource(R.string.order_asc),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (esami.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.libretto_vuoto),
                        modifier = Modifier.padding(horizontal = 32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(esami, key = { it.id }) { esame ->
                        EsameCard(
                            esame = esame,
                            onEditClick = { esameToEdit = esame },
                            onDeleteClick = { esameToDelete = esame }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.aggiungi_esame)
            )
        }
    }

    // Dialog aggiunta
    if (showAddDialog) {
        EsameDialog(
            title = stringResource(R.string.dialog_title_aggiungi),
            confirmLabel = stringResource(R.string.dialog_conferma),
            onDismiss = { showAddDialog = false },
            onConfirm = { esame ->
                viewModel.addEsame(esame)
                showAddDialog = false
            }
        )
    }

    // Dialog modifica
    esameToEdit?.let { esame ->
        EsameDialog(
            title = stringResource(R.string.dialog_title_modifica),
            confirmLabel = stringResource(R.string.dialog_salva),
            initialEsame = esame,
            onDismiss = { esameToEdit = null },
            onConfirm = { updated ->
                viewModel.updateEsame(updated)
                esameToEdit = null
            }
        )
    }

    // Dialog conferma eliminazione
    esameToDelete?.let { esame ->
        AlertDialog(
            onDismissRequest = { esameToDelete = null },
            title = { Text(stringResource(R.string.dialog_elimina_titolo)) },
            text = {
                Text(stringResource(R.string.dialog_elimina_testo, esame.nome))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEsame(esame)
                        esameToDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.dialog_conferma_elimina),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { esameToDelete = null }) {
                    Text(stringResource(R.string.dialog_annulla))
                }
            }
        )
    }
}

@Composable
private fun EsameDialog(
    title: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (Esame) -> Unit,
    initialEsame: Esame? = null
) {
    var nome by remember { mutableStateOf(initialEsame?.nome ?: "") }
    var voto by remember { mutableStateOf(initialEsame?.voto?.toString() ?: "") }
    var cfu by remember { mutableStateOf(initialEsame?.cfu?.toString() ?: "") }
    
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    var data by remember { mutableStateOf(initialEsame?.dataEsame?.format(dateFormatter) ?: "") }
    var lode by remember { mutableStateOf(initialEsame?.lode ?: false) }

    val votoInt = voto.toIntOrNull() ?: 0
    val cfuInt = cfu.toIntOrNull() ?: 0
    val cfuError = cfu.isNotEmpty() && (cfuInt < 1 || cfuInt > 48)

    // Auto-deseleziona lode se voto scende sotto 30
    LaunchedEffect(voto) {
        if (votoInt != 30 && lode) lode = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text(stringResource(R.string.hint_nome)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = voto,
                    onValueChange = { voto = it },
                    label = { Text(stringResource(R.string.hint_voto)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cfu,
                    onValueChange = { cfu = it },
                    label = { Text(stringResource(R.string.hint_cfu)) },
                    isError = cfuError,
                    supportingText = {
                        if (cfuError) Text(stringResource(R.string.errore_cfu_range))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text(stringResource(R.string.hint_data)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = lode,
                        onCheckedChange = { if (votoInt == 30) lode = it },
                        enabled = votoInt == 30
                    )
                    Text(
                        text = stringResource(R.string.label_lode),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (votoInt == 30) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nome.isBlank()) return@TextButton
                    if (votoInt !in 18..30) return@TextButton
                    if (cfuInt !in 1..48) return@TextButton
                    
                    val localDate = try {
                        LocalDate.parse(data.trim(), dateFormatter)
                    } catch (e: DateTimeParseException) {
                        return@TextButton
                    }

                    onConfirm(
                        Esame(
                            id = initialEsame?.id ?: 0,
                            nome = nome.trim(),
                            voto = votoInt,
                            lode = lode && votoInt == 30,
                            cfu = cfuInt,
                            dataEsame = localDate
                        )
                    )
                }
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_annulla))
            }
        }
    )
}
