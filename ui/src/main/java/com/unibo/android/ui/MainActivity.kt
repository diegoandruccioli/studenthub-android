package com.unibo.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unibo.android.domain.di.RepositoryProvider
import com.unibo.android.domain.usecase.GetObiettiviUseCase
import com.unibo.android.domain.usecase.GetSettingsUseCase
import com.unibo.android.domain.usecase.GetStatisticheUseCase
import com.unibo.android.domain.usecase.LogoutUseCase
import com.unibo.android.domain.usecase.UpdateSettingsUseCase
import com.unibo.android.ui.screens.auth.AuthViewModel
import com.unibo.android.ui.screens.auth.LoginScreen
import com.unibo.android.ui.screens.auth.RegisterScreen
import com.unibo.android.ui.screens.libretto.LibrettoScreen
import com.unibo.android.ui.screens.libretto.LibrettoViewModel
import com.unibo.android.ui.screens.obiettivi.ObiettiviScreen
import com.unibo.android.ui.screens.obiettivi.ObiettiviViewModel
import com.unibo.android.ui.screens.profilo.ProfiloScreen
import com.unibo.android.ui.screens.profilo.ProfiloViewModel
import com.unibo.android.ui.screens.statistiche.StatisticheScreen
import com.unibo.android.ui.screens.statistiche.StatisticheViewModel
import com.unibo.android.ui.theme.StudentHubTheme
import kotlinx.coroutines.Dispatchers
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentHubTheme {
                RootNavigation()
            }
        }
    }
}

@Composable
fun RootNavigation() {
    val context = LocalContext.current
    val authRepository = (context.applicationContext as RepositoryProvider).getAuthRepository()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(authRepository)
    )
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()
    var showRegister by rememberSaveable { mutableStateOf(false) }

    when (sessionState) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        false -> if (showRegister) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    authViewModel.resetState()
                    showRegister = false
                }
            )
        } else {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    authViewModel.resetState()
                    showRegister = true
                }
            )
        }
        true -> StudentHubApp()
    }
}

@Composable
fun StudentHubApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.LIBRETTO) }

    val context = LocalContext.current
    val repositoryProvider = context.applicationContext as RepositoryProvider
    val esameRepository = repositoryProvider.getEsameRepository()
    val obiettivoRepository = repositoryProvider.getObiettivoRepository()
    val settingsRepository = repositoryProvider.getSettingsRepository()
    val authRepository = repositoryProvider.getAuthRepository()

    val librettoViewModel: LibrettoViewModel = viewModel(
        factory = LibrettoViewModel.provideFactory(esameRepository, obiettivoRepository)
    )

    val statisticheViewModel: StatisticheViewModel = viewModel(
        factory = StatisticheViewModel.provideFactory(
            getStatisticheUseCase = GetStatisticheUseCase(
                repository = esameRepository,
                defaultDispatcher = Dispatchers.Default
            ),
            locale = Locale.getDefault()
        )
    )

    val obiettiviViewModel: ObiettiviViewModel = viewModel(
        factory = ObiettiviViewModel.provideFactory(
            getObiettiviUseCase = GetObiettiviUseCase(
                repository = obiettivoRepository
            )
        )
    )

    val profiloViewModel: ProfiloViewModel = viewModel(
        factory = ProfiloViewModel.provideFactory(
            getSettingsUseCase = GetSettingsUseCase(settingsRepository),
            updateSettingsUseCase = UpdateSettingsUseCase(settingsRepository),
            logoutUseCase = LogoutUseCase(authRepository)
        )
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        Scaffold(contentWindowInsets = WindowInsets.statusBars) { innerPadding ->
            when (currentDestination) {
                AppDestinations.LIBRETTO -> LibrettoScreen(
                    viewModel = librettoViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.STATISTICHE -> StatisticheScreen(
                    viewModel = statisticheViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.OBIETTIVI -> ObiettiviScreen(
                    viewModel = obiettiviViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                AppDestinations.PROFILO -> ProfiloScreen(
                    viewModel = profiloViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

enum class AppDestinations(val label: String, val icon: ImageVector) {
    LIBRETTO("Libretto", Icons.Outlined.School),
    STATISTICHE("Statistiche", Icons.Outlined.BarChart),
    OBIETTIVI("Obiettivi", Icons.Outlined.EmojiEvents),
    PROFILO("Profilo", Icons.Outlined.Person),
}

@PreviewScreenSizes
@Composable
private fun StudentHubNavigationPreview() {
    StudentHubTheme {
        var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.LIBRETTO) }
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        selected = destination == currentDestination,
                        onClick = { currentDestination = destination }
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentDestination.label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
