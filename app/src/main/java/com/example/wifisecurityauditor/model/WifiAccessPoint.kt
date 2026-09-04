package com.example.wifisecurityauditor.model

data class WifiAccessPoint(
    val bssid: String,
    val ssid: String,
    val rssi: Int,
    val frequency: Int,
    val channel: Int,
    val band: String,
    val capabilities: String,
    val primarySecurity: String,
    val securityGrade: SecurityGrade,
    val isWpsSupported: Boolean,
    val encryptionType: String,
    val recommendations: List<String>
)

