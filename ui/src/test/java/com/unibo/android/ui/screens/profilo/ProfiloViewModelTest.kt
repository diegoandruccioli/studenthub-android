package com.unibo.android.ui.screens.profilo

import com.unibo.android.domain.model.Settings
import com.unibo.android.domain.usecase.GetSettingsUseCase
import com.unibo.android.domain.usecase.LogoutUseCase
import com.unibo.android.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfiloViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getSettings: GetSettingsUseCase = mock()
    private val updateSettings: UpdateSettingsUseCase = mock()
    private val logout: LogoutUseCase = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private val defaultSettings = Settings("STANDARD", 18, 30)

    @Test
    fun `init - carica settings e passa a Success`() = runTest {
        whenever(getSettings()).thenReturn(Result.success(defaultSettings))

        val vm = ProfiloViewModel(getSettings, updateSettings, logout)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is ProfiloUiState.Success)
        assertEquals(defaultSettings, (state as ProfiloUiState.Success).settings)
    }

    @Test
    fun `init - errore API passa a Error`() = runTest {
        whenever(getSettings()).thenReturn(Result.failure(Exception("Network error")))

        val vm = ProfiloViewModel(getSettings, updateSettings, logout)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is ProfiloUiState.Error)
    }

    @Test
    fun `saveSettings - successo porta a Success con settings aggiornate`() = runTest {
        whenever(getSettings()).thenReturn(Result.success(defaultSettings))
        whenever(updateSettings(defaultSettings)).thenReturn(Result.success(Unit))

        val vm = ProfiloViewModel(getSettings, updateSettings, logout)
        advanceUntilIdle()

        vm.saveSettings(defaultSettings)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is ProfiloUiState.Success)
        assertEquals(defaultSettings, (state as ProfiloUiState.Success).settings)
        verify(updateSettings)(defaultSettings)
    }

    @Test
    fun `saveSettings - errore API porta a Error`() = runTest {
        whenever(getSettings()).thenReturn(Result.success(defaultSettings))
        whenever(updateSettings(defaultSettings))
            .thenReturn(Result.failure(Exception("Salvataggio fallito")))

        val vm = ProfiloViewModel(getSettings, updateSettings, logout)
        advanceUntilIdle()
        vm.saveSettings(defaultSettings)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is ProfiloUiState.Error)
        assertEquals("Salvataggio fallito", (state as ProfiloUiState.Error).message)
    }

    @Test
    fun `logout - isLoggingOut true durante operazione poi false`() = runTest {
        whenever(getSettings()).thenReturn(Result.success(defaultSettings))
        whenever(logout()).thenReturn(Result.success(Unit))

        val vm = ProfiloViewModel(getSettings, updateSettings, logout)
        advanceUntilIdle()

        assertFalse(vm.isLoggingOut.value)
        vm.logout()
        advanceUntilIdle()

        assertFalse(vm.isLoggingOut.value)
        verify(logout)()
    }
}
