package com.securitynav.security.data

data class AppTrafficItem(
    val appName: String,
    val packageName: String,
    val bytesText: String,
    val httpVerb: String
)
