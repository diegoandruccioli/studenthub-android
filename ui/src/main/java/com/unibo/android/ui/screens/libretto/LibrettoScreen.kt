package com.unibo.android.ui.screens.libretto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unibo.android.domain.model.Esame
import com.unibo.android.ui.R
import com.unibo.android.ui.theme.StudentHubTheme

@Composable
fun LibrettoScreen(
    viewModel: LibrettoViewModel,
    modifier: Modifier = Modifier
) {
    val esami by viewModel.esami.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (esami.isEmpty()) {
            Text(
                text = stringResource(R.string.libretto_vuoto),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(esami, key = { it.id }) { esame ->
                    EsameCard(
                        esame = esame,
                        onDeleteClick = { viewModel.deleteEsame(esame) }
                    )
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

    if (showAddDialog) {
        AddEsameDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { esame ->
                viewModel.addEsame(esame)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddEsameDialog(
    onDismiss: () -> Unit,
    onConfirm: (Esame) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var voto by remember { mutableStateOf("") }
    var cfu by remember { mutableStateOf("") }
    var data by remember { mutableStateOf("") }
    var lode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_title_aggiungi)) },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = lode, onCheckedChange = { lode = it })
                    Text(
                        text = stringResource(R.string.label_lode),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val votoInt = voto.toIntOrNull() ?: return@TextButton
                    val cfuInt = cfu.toIntOrNull() ?: return@TextButton
                    if (nome.isNotBlank() && votoInt in 18..30 && cfuInt > 0) {
                        onConfirm(
                            Esame(
                                nome = nome.trim(),
                                voto = votoInt,
                                lode = lode && votoInt == 30,
                                cfu = cfuInt,
                                dataEsame = data.trim()
                            )
                        )
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_conferma))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_annulla))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AddEsameDialogPreview() {
    StudentHubTheme {
        AddEsameDialog(onDismiss = {}, onConfirm = {})
    }
}
