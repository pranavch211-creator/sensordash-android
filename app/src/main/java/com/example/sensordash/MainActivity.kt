package com.example.sensordash

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Phase 1 shell: proves the toolchain, CI build and install path. No gauges or sensors yet. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShellScreen() }
    }
}

@Composable
private fun ShellScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF04070D)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BasicText(
                text = "SensorDash",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                ),
            )
            Spacer(Modifier.height(8.dp))
            BasicText(
                text = "Native Android shell - Phase 1",
                style = TextStyle(color = Color(0xFF27B4FF), fontSize = 20.sp),
            )
            Spacer(Modifier.height(16.dp))
            BasicText(
                text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) - ${Build.MODEL}",
                style = TextStyle(color = Color(0xFF8FA3BD), fontSize = 14.sp),
            )
        }
    }
}
