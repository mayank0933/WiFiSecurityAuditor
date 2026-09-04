package com.example.wifisecurityauditor.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wifisecurityauditor.data.ScanStatus
import com.example.wifisecurityauditor.data.WifiScanRepository
import com.example.wifisecurityauditor.model.SecurityGrade
import com.example.wifisecurityauditor.model.WifiAccessPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOption {
    SIGNAL_STRENGTH,
    SECURITY_RISK,
    ALPHABETICAL
}

data class ScannerUiState(
    val isLoading: Boolean = false,
    val networks: List<WifiAccessPoint> = emptyList(),
    val selectedNetwork: WifiAccessPoint? = null,
    val errorMessage: String? = null,
    val isThrottled: Boolean = false,
    val sortOption: SortOption = SortOption.SIGNAL_STRENGTH
)

class WifiScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WifiScanRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun triggerScan() {
        viewModelScope.launch {
            repository.startScan().collect { status ->
                when (status) {
                    is ScanStatus.Idle -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is ScanStatus.Scanning -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is ScanStatus.Success -> {
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                networks = sortList(status.networks, current.sortOption),
                                isThrottled = status.isThrottled,
                                errorMessage = null
                            )
                        }
                    }
                    is ScanStatus.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = status.message) }
                    }
                }
            }
        }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { current ->
            current.copy(
                sortOption = option,
                networks = sortList(current.networks, option)
            )
        }
    }

    fun selectNetwork(network: WifiAccessPoint?) {
        _uiState.update { it.copy(selectedNetwork = network) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun sortList(list: List<WifiAccessPoint>, option: SortOption): List<WifiAccessPoint> {
        return when (option) {
            SortOption.SIGNAL_STRENGTH -> list.sortedByDescending { it.rssi }
            SortOption.SECURITY_RISK -> list.sortedWith(
                compareBy<WifiAccessPoint> {
                    when (it.securityGrade) {
                        SecurityGrade.CRITICAL -> 0
                        SecurityGrade.HIGH -> 1
                        SecurityGrade.MODERATE -> 2
                        SecurityGrade.SECURE -> 3
                    }
                }.thenByDescending { it.rssi }
            )
            SortOption.ALPHABETICAL -> list.sortedBy { it.ssid.lowercase() }
        }
    }
}

