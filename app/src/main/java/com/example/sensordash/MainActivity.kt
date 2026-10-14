package com.example.sensordash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sensordash.data.DeviceMonitor
import com.example.sensordash.ui.DashboardScreen

/** Phase 3: live battery, memory, app CPU, clock, device, display and network. Motion/GPS/mic are still demo. */
class MainActivity : ComponentActivity() {
    private lateinit var monitor: DeviceMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        monitor = DeviceMonitor(this)
        setContent { DashboardScreen(monitor.state) }
    }

    override fun onStart() {
        super.onStart()
        monitor.start()
    }

    override fun onStop() {
        monitor.stop()
        super.onStop()
    }
}
