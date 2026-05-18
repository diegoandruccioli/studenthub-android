package com.unibo.android.ui.screens.profilo

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unibo.android.domain.model.Settings
import com.unibo.android.ui.R

private val TEMI = listOf("STANDARD", "LODE", "RGB")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfiloScreen(
    viewModel: ProfiloViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggingOut by viewModel.isLoggingOut.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is ProfiloUiState.Error) {
            snackbarHostState.showSnackbar((uiState as ProfiloUiState.Error).message)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProfiloUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ProfiloUiState.Success -> {
                ProfiloContent(
                    settings = state.settings,
                    isLoggingOut = isLoggingOut,
                    isSaving = false,
                    onSave = { viewModel.saveSettings(it) },
                    onLogout = { viewModel.logout() }
                )
            }
            is ProfiloUiState.Saving -> {
                ProfiloContent(
                    settings = state.previousSettings,
                    isLoggingOut = isLoggingOut,
                    isSaving = true,
                    onSave = {},
                    onLogout = { viewModel.logout() }
                )
            }
            is ProfiloUiState.Error -> {
                ProfiloContent(
                    settings = Settings("STANDARD", 18, 30),
                    isLoggingOut = isLoggingOut,
                    isSaving = false,
                    onSave = { viewModel.saveSettings(it) },
                    onLogout = { viewModel.logout() }
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfiloContent(
    settings: Settings,
    isLoggingOut: Boolean,
    isSaving: Boolean,
    onSave: (Settings) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTema by remember(settings.temaVoti) { mutableStateOf(settings.temaVoti) }
    var sogliaBassa by remember(settings.rgbSogliaBassa) { mutableStateOf(settings.rgbSogliaBassa.toString()) }
    var sogliaAlta by remember(settings.rgbSogliaAlta) { mutableStateOf(settings.rgbSogliaAlta.toString()) }
    var temaExpanded by remember { mutableStateOf(false) }

    val sogliaBassaInt = sogliaBassa.toIntOrNull() ?: 18
    val sogliaAltaInt = sogliaAlta.toIntOrNull() ?: 30
    val soglieBassaError = selectedTema == "RGB" && sogliaBassa.isNotEmpty() &&
            (sogliaBassaInt < 18 || sogliaBassaInt > 30)
    val soglieAltaError = selectedTema == "RGB" && sogliaAlta.isNotEmpty() &&
            (sogliaAltaInt < 18 || sogliaAltaInt > 30 || sogliaAltaInt <= sogliaBassaInt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.profilo_breadcrumb),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.profilo_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = stringResource(R.string.profilo_subtitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.profilo_impostazioni_titolo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = temaExpanded,
                    onExpandedChange = { if (!isSaving) temaExpanded = it }
                ) {
                    OutlinedTextField(
                        value = when (selectedTema) {
                            "LODE" -> stringResource(R.string.profilo_tema_lode)
                            "RGB" -> stringResource(R.string.profilo_tema_rgb)
                            else -> stringResource(R.string.profilo_tema_standard)
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.profilo_tema_voti_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = temaExpanded) },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = temaExpanded,
                        onDismissRequest = { temaExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.profilo_tema_standard)) },
                            onClick = { selectedTema = "STANDARD"; temaExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.profilo_tema_lode)) },
                            onClick = { selectedTema = "LODE"; temaExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.profilo_tema_rgb)) },
                            onClick = { selectedTema = "RGB"; temaExpanded = false }
                        )
                    }
                }

                if (selectedTema == "RGB") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sogliaBassa,
                            onValueChange = { sogliaBassa = it },
                            label = { Text(stringResource(R.string.profilo_soglia_bassa)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = soglieBassaError,
                            enabled = !isSaving,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sogliaAlta,
                            onValueChange = { sogliaAlta = it },
                            label = { Text(stringResource(R.string.profilo_soglia_alta)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = soglieAltaError,
                            enabled = !isSaving,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (soglieBassaError || soglieAltaError) {
                        Text(
                            text = stringResource(R.string.profilo_errore_soglie),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Button(
                    onClick = {
                        onSave(
                            Settings(
                                temaVoti = selectedTema,
                                rgbSogliaBassa = if (selectedTema == "RGB") sogliaBassaInt else 18,
                                rgbSogliaAlta = if (selectedTema == "RGB") sogliaAltaInt else 30
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving && !soglieBassaError && !soglieAltaError
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.profilo_salva))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.profilo_account_titolo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoggingOut,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isLoggingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(stringResource(R.string.profilo_logout))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}
