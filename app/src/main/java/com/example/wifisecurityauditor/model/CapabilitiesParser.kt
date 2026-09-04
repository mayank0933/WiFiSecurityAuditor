package com.example.wifisecurityauditor.model

import android.net.wifi.ScanResult

object CapabilitiesParser {

    fun parse(scanResult: ScanResult): WifiAccessPoint {
        val caps = scanResult.capabilities ?: ""
        val isWps = caps.contains("WPS", ignoreCase = true)

        val (primarySec, grade, encType, recommendations) = evaluateSecurity(caps, isWps)
        val channel = frequencyToChannel(scanResult.frequency)
        val band = frequencyToBand(scanResult.frequency)

        return WifiAccessPoint(
            bssid = scanResult.BSSID ?: "Unknown",
            ssid = if (scanResult.SSID.isNullOrBlank()) "[Hidden Network]" else scanResult.SSID,
            rssi = scanResult.level,
            frequency = scanResult.frequency,
            channel = channel,
            band = band,
            capabilities = caps,
            primarySecurity = primarySec,
            securityGrade = grade,
            isWpsSupported = isWps,
            encryptionType = encType,
            recommendations = recommendations
        )
    }

    private fun evaluateSecurity(
        capabilities: String,
        isWps: Boolean
    ): SecurityAnalysis {
        val recs = mutableListOf<String>()
        val upperCaps = capabilities.uppercase()

        val primarySecurity: String
        val grade: SecurityGrade
        val encryption: String

        when {
            upperCaps.contains("SAE") || upperCaps.contains("WPA3") -> {
                primarySecurity = "WPA3-Personal (SAE)"
                grade = SecurityGrade.SECURE
                encryption = "AES / GCMP-256"
                recs.add("Network configuration follows state-of-the-art security standards.")
                recs.add("Ensure Protected Management Frames (PMF) are strictly enforced.")
            }
            upperCaps.contains("WPA2") || upperCaps.contains("RSN") -> {
                primarySecurity = "WPA2-PSK"
                grade = SecurityGrade.MODERATE
                encryption = if (upperCaps.contains("TKIP")) "TKIP / CCMP Mixed" else "AES-CCMP"
                
                if (upperCaps.contains("TKIP")) {
                    recs.add("TKIP is deprecated and reduces throughput. Disable TKIP in router settings and force pure AES/CCMP.")
                }
                recs.add("Upgrade to WPA3-Personal or WPA2/WPA3 transitional mode if hardware allows.")
                recs.add("Ensure the Pre-Shared Key (PSK) contains at least 16+ high-entropy characters.")
            }
            upperCaps.contains("WPA-") -> {
                primarySecurity = "WPA (Legacy)"
                grade = SecurityGrade.HIGH
                encryption = "TKIP"
                recs.add("WPA1 is obsolete and susceptible to packet forgery attacks (Beck-Tews / Ohigashi-Kuwakado).")
                recs.add("Immediately reconfigure the Access Point to WPA2 or WPA3.")
            }
            upperCaps.contains("WEP") -> {
                primarySecurity = "WEP (Insecure)"
                grade = SecurityGrade.CRITICAL
                encryption = "RC4 (Broken)"
                recs.add("WEP has fundamental cryptographic flaws in RC4 initialization vectors (FMS / KoreK attacks).")
                recs.add("Replace or reconfigure this access point immediately. WEP keys can be cracked in seconds.")
            }
            else -> {
                primarySecurity = "Open (None)"
                grade = SecurityGrade.CRITICAL
                encryption = "None"
                recs.add("Zero encryption on wireless medium: All unencrypted HTTP/DNS traffic can be sniffed by anyone nearby.")
                recs.add("Enable WPA2/WPA3 password protection or implement an encrypted VPN tunnel for all traffic.")
            }
        }

        if (isWps) {
            recs.add("WPS (Wi-Fi Protected Setup) is detected. WPS PIN authentication is susceptible to offline brute-force attacks (Reaver/Pixie Dust). Disable WPS in router firmware.")
        }

        return SecurityAnalysis(primarySecurity, grade, encryption, recs)
    }

    private data class SecurityAnalysis(
        val primarySecurity: String,
        val grade: SecurityGrade,
        val encryption: String,
        val recommendations: List<String>
    )

    private fun frequencyToChannel(freq: Int): Int {
        return when {
            freq == 2484 -> 14
            freq in 2412..2472 -> (freq - 2412) / 5 + 1
            freq in 5170..5825 -> (freq - 5170) / 5 + 34
            freq in 5925..7125 -> (freq - 5925) / 5 + 1 // Wi-Fi 6E
            else -> 0
        }
    }

    private fun frequencyToBand(freq: Int): String {
        return when {
            freq in 2400..2495 -> "2.4 GHz"
            freq in 5150..5895 -> "5.0 GHz"
            freq in 5925..7125 -> "6.0 GHz"
            else -> "Unknown Band"
        }
    }
}

