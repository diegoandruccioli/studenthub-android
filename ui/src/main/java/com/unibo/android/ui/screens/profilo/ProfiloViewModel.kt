package com.unibo.android.ui.screens.profilo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unibo.android.domain.model.Settings
import com.unibo.android.domain.repository.SettingsRepository
import com.unibo.android.domain.usecase.GetSettingsUseCase
import com.unibo.android.domain.usecase.LogoutUseCase
import com.unibo.android.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfiloViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfiloUiState>(ProfiloUiState.Loading)
    val uiState: StateFlow<ProfiloUiState> = _uiState.asStateFlow()

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

    val lastCheckTimestamp: StateFlow<Long> = settingsRepository.observeLastCheckTimestamp()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val currentRank: StateFlow<Int> = settingsRepository.observeLocalRank()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = ProfiloUiState.Loading
            val result = getSettingsUseCase()
            _uiState.value = result.fold(
                onSuccess = { ProfiloUiState.Success(it) },
                onFailure = { ProfiloUiState.Error(it.message ?: "Errore sconosciuto") }
            )
        }
    }

    fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            _uiState.value = ProfiloUiState.Saving(settings)
            val result = updateSettingsUseCase(settings)
            _uiState.value = result.fold(
                onSuccess = { ProfiloUiState.Success(settings) },
                onFailure = { ProfiloUiState.Error(it.message ?: "Errore nel salvataggio", settings) }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoggingOut.value = true
            logoutUseCase()
            _isLoggingOut.value = false
        }
    }

    fun triggerTestNotification() {
        viewModelScope.launch {
            settingsRepository.triggerTestNotification()
        }
    }

    fun runLeaderboardWorkerNow() {
        viewModelScope.launch {
            settingsRepository.runLeaderboardWorkerNow()
        }
    }

    fun simulateRankChange(newRank: Int) {
        viewModelScope.launch {
            settingsRepository.setLocalRank(newRank)
        }
    }

    companion object {
        fun provideFactory(
            getSettingsUseCase: GetSettingsUseCase,
            updateSettingsUseCase: UpdateSettingsUseCase,
            logoutUseCase: LogoutUseCase,
            settingsRepository: SettingsRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfiloViewModel(
                    getSettingsUseCase = getSettingsUseCase,
                    updateSettingsUseCase = updateSettingsUseCase,
                    logoutUseCase = logoutUseCase,
                    settingsRepository = settingsRepository
                ) as T
        }
    }
}
