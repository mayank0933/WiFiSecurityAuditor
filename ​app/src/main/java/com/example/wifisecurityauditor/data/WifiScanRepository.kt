package com.example.wifisecurityauditor.data

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.WifiManager
import com.example.wifisecurityauditor.model.CapabilitiesParser
import com.example.wifisecurityauditor.model.WifiAccessPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed class ScanStatus {
    object Idle : ScanStatus()
    object Scanning : ScanStatus()
    data class Success(val networks: List<WifiAccessPoint>, val isThrottled: Boolean) : ScanStatus()
    data class Error(val message: String) : ScanStatus()
}

class WifiScanRepository(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val locationManager = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun isWifiEnabled(): Boolean = wifiManager?.isWifiEnabled == true

    fun isLocationEnabled(): Boolean {
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
               locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }

    @SuppressLint("MissingPermission")
    fun startScan(): Flow<ScanStatus> = callbackFlow {
        if (wifiManager == null) {
            trySend(ScanStatus.Error("Wi-Fi hardware service not available on this device."))
            close()
            return@callbackFlow
        }

        if (!isWifiEnabled()) {
            trySend(ScanStatus.Error("Wi-Fi is currently disabled. Please enable Wi-Fi in system settings."))
            close()
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            trySend(ScanStatus.Error("Location services are disabled. Android requires Location to be ON for Wi-Fi discovery."))
            close()
            return@callbackFlow
        }

        trySend(ScanStatus.Scanning)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val updated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    try {
                        val scanResults = wifiManager.scanResults ?: emptyList()
                        val parsedNetworks = scanResults.map { CapabilitiesParser.parse(it) }
                            .distinctBy { it.bssid }

                        trySend(ScanStatus.Success(parsedNetworks, isThrottled = !updated))
                    } catch (e: SecurityException) {
                        trySend(ScanStatus.Error("Permission denied while reading scan results: ${e.message}"))
                    }
                }
            }
        }

        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(receiver, filter)

        // Request a new scan from the OS
        @Suppress("DEPRECATION")
        val scanInitiated = wifiManager.startScan()
        if (!scanInitiated) {
            // Even if startScan returns false (often due to OS scan throttling),
            // existing cached scan results can still be read.
            try {
                val cached = wifiManager.scanResults ?: emptyList()
                val parsed = cached.map { CapabilitiesParser.parse(it) }.distinctBy { it.bssid }
                trySend(ScanStatus.Success(parsed, isThrottled = true))
            } catch (e: Exception) {
                trySend(ScanStatus.Error("Scan throttled by Android OS. Please wait a moment before re-scanning."))
            }
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) {
                // Receiver was not registered or already unregistered
            }
        }
    }
}

