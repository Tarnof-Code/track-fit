package com.sport.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sport.gymtracker.R
import com.sport.gymtracker.ui.navigation.GymTrackerNavHost
import com.sport.gymtracker.ui.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymTracker)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GymTrackerRoot() }
    }
}

@Composable
private fun GymTrackerRoot() {
    val defaultToSystemDark = isSystemInDarkTheme()
    var darkTheme by rememberSaveable { mutableStateOf(defaultToSystemDark) }
    GymTrackerTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            GymTrackerNavHost(
                darkTheme = darkTheme,
                onToggleDarkTheme = { darkTheme = !darkTheme },
            )
        }
    }
}
