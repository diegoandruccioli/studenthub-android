package com.unibo.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unibo.android.ui.screens.auth.AuthViewModel
import com.unibo.android.ui.screens.auth.LoginScreen
import com.unibo.android.ui.screens.auth.RegisterScreen
import com.unibo.android.ui.screens.libretto.LibrettoScreen
import com.unibo.android.ui.screens.libretto.LibrettoViewModel
import com.unibo.android.ui.screens.obiettivi.ObiettiviScreen
import com.unibo.android.ui.screens.obiettivi.ObiettiviViewModel
import com.unibo.android.ui.screens.statistiche.StatisticheScreen
import com.unibo.android.ui.screens.statistiche.StatisticheViewModel
import com.unibo.android.ui.theme.StudentHubTheme

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
    val authViewModel: AuthViewModel = viewModel()
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
    val librettoViewModel: LibrettoViewModel = viewModel()
    val statisticheViewModel: StatisticheViewModel = viewModel()
    val obiettiviViewModel: ObiettiviViewModel = viewModel()

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
        when (currentDestination) {
            AppDestinations.LIBRETTO -> LibrettoScreen(viewModel = librettoViewModel)
            AppDestinations.STATISTICHE -> StatisticheScreen(viewModel = statisticheViewModel)
            AppDestinations.OBIETTIVI -> ObiettiviScreen(viewModel = obiettiviViewModel)
        }
    }
}

enum class AppDestinations(val label: String, val icon: ImageVector) {
    LIBRETTO("Libretto", Icons.Outlined.School),
    STATISTICHE("Statistiche", Icons.Outlined.BarChart),
    OBIETTIVI("Obiettivi", Icons.Outlined.EmojiEvents),
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
                    text = "${currentDestination.label} — prossimamente",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
