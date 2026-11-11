package com.trix.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trix.calculator.ui.screens.*
import com.trix.calculator.ui.theme.DarkBackground
import com.trix.calculator.ui.theme.TrixCalculatorTheme
import com.trix.calculator.ui.viewmodel.AppScreen
import com.trix.calculator.ui.viewmodel.TrixViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrixCalculatorTheme {
                // تفعيل اتجاه اليمين إلى اليسار (RTL) للواجهة العربية بالكامل
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = DarkBackground
                    ) {
                        TrixApp()
                    }
                }
            }
        }
    }
}

@Composable
fun TrixApp(viewModel: TrixViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState.currentScreen) {
        AppScreen.CONFIG -> {
            ConfigScreen(state = uiState, viewModel = viewModel)
        }
        AppScreen.GAME_DASHBOARD -> {
            GameDashboardScreen(state = uiState, viewModel = viewModel)
        }
        AppScreen.GAME_OVER -> {
            GameOverScreen(state = uiState, viewModel = viewModel)
        }
    }

    // النوافذ المنبثقة التفاعلية
    RoundInputDialog(state = uiState, viewModel = viewModel)
    HistoryDialog(state = uiState, viewModel = viewModel)
    RulesHelpDialog(state = uiState, viewModel = viewModel)
}
