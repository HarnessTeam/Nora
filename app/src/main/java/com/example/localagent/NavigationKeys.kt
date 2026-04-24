package com.example.localagent

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Navigation keys
@Serializable
data object Setup : NavKey

@Serializable
data object Chat : NavKey
