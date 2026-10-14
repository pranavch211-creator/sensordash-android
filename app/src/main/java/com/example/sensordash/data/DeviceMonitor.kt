package com.example.sensordash.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Owns all Phase 3 providers. One 1 Hz ticker (aligned to the second) drives clock and CPU; memory, device
 * and display refresh every 2 s; battery every 5 s; network is event-driven. start()/stop() are called
 * from the Activity's onStart/onStop, so nothing runs while the app is in the background.
 */
class DeviceMonitor(private val ctx: Context) {
    var state by mutableStateOf(DeviceState())
        private set

    private val handler = Handler(Looper.getMainLooper())
    private val cpu = CpuSampler()
    private val network = NetworkWatcher(ctx) { n -> state = state.copy(network = n) }
    private var running = false
    private var ticks = 0

    private val ticker = object : Runnable {
        override fun run() {
            if (!running) return
            tick()
            handler.postDelayed(this, 1000L - System.currentTimeMillis() % 1000L)
        }
    }

    fun start() {
        if (running) return
        running = true
        ticks = 0
        cpu.reset()
        network.start()
        tick()
        handler.postDelayed(ticker, 1000L - System.currentTimeMillis() % 1000L)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(ticker)
        network.stop()
    }

    private fun tick() {
        val n = ticks++
        var s = state.copy(clock = readClock(), cpu = cpu.sample())
        if (n % 2 == 0) s = s.copy(memory = readMemory(ctx), device = readDevice(ctx), display = readDisplay(ctx))
        if (n % 5 == 0) s = s.copy(battery = readBattery(ctx))
        state = s
    }
}
