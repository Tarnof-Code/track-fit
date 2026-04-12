package com.sport.gymtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sport.gymtracker.R
import com.sport.gymtracker.ui.navigation.GymTrackerNavHost
import com.sport.gymtracker.ui.theme.GymTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GymTracker)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GymTrackerNavHost()
                }
            }
        }
    }
}
