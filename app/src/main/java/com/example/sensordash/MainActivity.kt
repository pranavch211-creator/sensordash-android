package com.example.sensordash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sensordash.ui.DashboardScreen

/** Phase 2: the dashboard UI with fixed demo placeholders. Sensors arrive in later phases. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DashboardScreen() }
    }
}
