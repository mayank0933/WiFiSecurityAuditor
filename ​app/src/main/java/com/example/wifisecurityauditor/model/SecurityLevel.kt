package com.example.wifisecurityauditor.model

import androidx.compose.ui.graphics.Color

enum class SecurityGrade(
    val label: String,
    val color: Color,
    val riskDescription: String
) {
    CRITICAL(
        label = "Critical Risk",
        color = Color(0xFFD32F2F),
        riskDescription = "Network is completely unencrypted or uses broken algorithms (WEP). Traffic can be intercepted effortlessly."
    ),
    HIGH(
        label = "High Risk",
        color = Color(0xFFF57C00),
        riskDescription = "Uses deprecated protocols like WPA1 with TKIP. Vulnerable to handshake packet injection and offline dictionary attacks."
    ),
    MODERATE(
        label = "Moderate",
        color = Color(0xFFFBC02D),
        riskDescription = "Standard WPA2-PSK (AES-CCMP). Secure against passive sniffing, but vulnerable to dictionary attacks if a weak pre-shared key is used or WPS is enabled."
    ),
    SECURE(
        label = "Secure",
        color = Color(0xFF388E3C),
        riskDescription = "Modern WPA3-SAE. Employs Simultaneous Authentication of Equals to eliminate offline dictionary attacks and provide forward secrecy."
    )
}

