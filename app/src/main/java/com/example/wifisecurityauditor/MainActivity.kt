package com.example.wifisecurityauditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.wifisecurityauditor.ui.*
import com.example.wifisecurityauditor.ui.theme.WiFiSecurityAuditorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WifiScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WiFiSecurityAuditorTheme {
                var permissionsGranted by remember { mutableStateOf(false) }
                val uiState by viewModel.uiState.collectAsState()

                if (!permissionsGranted) {
                    PermissionHandler(
                        onPermissionsGranted = {
                            permissionsGranted = true
                            viewModel.triggerScan()
                        }
                    )
                } else {
                    val selected = uiState.selectedNetwork
                    if (selected != null) {
                        DetailScreen(
                            network = selected,
                            onBack = { viewModel.selectNetwork(null) }
                        )
                    } else {
                        ScannerScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
