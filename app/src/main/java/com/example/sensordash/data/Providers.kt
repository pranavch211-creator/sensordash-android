package com.example.sensordash.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.WindowManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

// ---------------- Battery: sticky ACTION_BATTERY_CHANGED + BatteryManager ----------------

fun readBattery(ctx: Context): BatteryState {
    val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        ?: return BatteryState(null, "Unavailable")
    val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (level < 0 || scale <= 0) return BatteryState(null, "Unavailable")
    val pct = (level * 100f / scale).roundToInt().coerceIn(0, 100)
    val text = when (i.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)) {
        BatteryManager.BATTERY_STATUS_CHARGING -> chargingText(ctx)
        BatteryManager.BATTERY_STATUS_FULL -> "Fully charged"
        BatteryManager.BATTERY_STATUS_DISCHARGING,
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "On battery"
        else -> "Status unknown"
    }
    return BatteryState(pct, text)
}

/** Android gives a time-to-full estimate only while charging (API 28+); there is no public time-to-empty. */
private fun chargingText(ctx: Context): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val ms = ctx.getSystemService(BatteryManager::class.java)?.computeChargeTimeRemaining() ?: -1L
        if (ms > 0L) {
            val m = ms / 60_000L
            return "Full in " + if (m < 60) "${m}m" else "${m / 60}h ${m % 60}m"
        }
    }
    return "Charging"
}

// ---------------- System RAM: ActivityManager.MemoryInfo ----------------

/** used = totalMem - availMem. availMem includes reclaimable memory, so the result is approximate. */
fun readMemory(ctx: Context): MemoryState {
    val am = ctx.getSystemService(ActivityManager::class.java) ?: return MemoryState(null, "Unavailable")
    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)
    if (mi.totalMem <= 0L) return MemoryState(null, "Unavailable")
    val used = (mi.totalMem - mi.availMem).coerceIn(0L, mi.totalMem)
    val gib = 1024f * 1024f * 1024f
    val detail = "approx. %.1f / %.1f GB".format(Locale.US, used / gib, mi.totalMem / gib)
    return MemoryState(used * 100f / mi.totalMem, detail)
}

// ---------------- APP CPU: Process.getElapsedCpuTime() vs SystemClock.elapsedRealtime() ----------------

/**
 * CPU time used by THIS app process (all threads) divided by elapsed wall time, per sample window.
 * Scale: 100% = this app keeps one full core busy. On an 8-core tablet the theoretical maximum is 800%;
 * the dial ends at 100 and the readout still shows the real number. This is NOT whole-tablet CPU usage.
 * Samples are rejected (null) for the first window, for windows shorter than 200 ms, or if CPU time went backwards.
 */
class CpuSampler {
    private var lastCpu = -1L
    private var lastWall = -1L
    private val cores = Runtime.getRuntime().availableProcessors()

    fun reset() { lastCpu = -1L; lastWall = -1L }

    fun sample(): CpuState {
        val cpu = Process.getElapsedCpuTime()
        val wall = SystemClock.elapsedRealtime()
        val pc = lastCpu
        val pw = lastWall
        lastCpu = cpu
        lastWall = wall
        if (pc < 0L || pw < 0L) return CpuState(null, "measuring...")
        val dc = cpu - pc
        val dw = wall - pw
        if (dw < 200L || dc < 0L) return CpuState(null, "no valid sample")
        return CpuState(dc * 100f / dw, "of 1 core, $cores cores")
    }
}

// ---------------- Clock: java.time local time ----------------

private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss")
private val DATE_FMT = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val DAY_FMT = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())

fun readClock(): ClockState {
    val n = LocalDateTime.now()
    return ClockState(n.hour, n.minute, n.second, TIME_FMT.format(n), DATE_FMT.format(n), DAY_FMT.format(n))
}

// ---------------- Device + Display: Build, Resources, WindowManager ----------------

fun readDevice(ctx: Context): DeviceInfoState {
    val cfg = ctx.resources.configuration
    val mfr = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    val model = if (Build.MODEL.startsWith(Build.MANUFACTURER, ignoreCase = true)) Build.MODEL else "$mfr ${Build.MODEL}"
    return DeviceInfoState(
        platform = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        model = model,
        language = Locale.getDefault().toLanguageTag(),
        screenDp = "${cfg.screenWidthDp} × ${cfg.screenHeightDp} dp",
    )
}

fun readDisplay(ctx: Context): DisplayState {
    val wm = ctx.getSystemService(WindowManager::class.java)
    val w: Int
    val h: Int
    if (wm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val b = wm.maximumWindowMetrics.bounds
        w = b.width(); h = b.height()
    } else {
        @Suppress("DEPRECATION")
        val m = DisplayMetrics().also { wm?.defaultDisplay?.getRealMetrics(it) }
        w = m.widthPixels; h = m.heightPixels
    }
    val density = ctx.resources.displayMetrics.density
    val orient = when (ctx.resources.configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
        Configuration.ORIENTATION_PORTRAIT -> "Portrait"
        else -> "Unknown"
    }
    return DisplayState("$w × $h", String.format(Locale.US, "%.2fx", density), orient)
}

// ---------------- Network: ConnectivityManager + NetworkCapabilities ----------------

class NetworkWatcher(ctx: Context, private val onChange: (NetworkState) -> Unit) {
    private val cm: ConnectivityManager? = ctx.getSystemService(ConnectivityManager::class.java)
    private val main = Handler(Looper.getMainLooper())
    private var callback: ConnectivityManager.NetworkCallback? = null

    private fun post(s: NetworkState) { main.post { onChange(s) } }

    fun start() {
        val m = cm
        if (m == null || callback != null) {
            if (m == null) onChange(unavailable())
            return
        }
        try {
            val caps = m.activeNetwork?.let { m.getNetworkCapabilities(it) }
            onChange(if (caps != null) describe(caps) else offline())
            val cb = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    post(describe(networkCapabilities))
                }
                override fun onLost(network: Network) { post(offline()) }
            }
            m.registerDefaultNetworkCallback(cb)
            callback = cb
        } catch (e: Exception) {
            onChange(unavailable())
        }
    }

    fun stop() {
        callback?.let { try { cm?.unregisterNetworkCallback(it) } catch (e: Exception) { } }
        callback = null
    }

    private fun offline() = NetworkState("None", "Offline", false, "Unavailable", "Unavailable")
    private fun unavailable() = NetworkState("Unavailable", "Unavailable", false, "Unavailable", "Unavailable")

    private fun describe(c: NetworkCapabilities): NetworkState {
        val type = when {
            c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            c.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            c.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
            c.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Other"
        }
        val internet = c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val validated = c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val status = if (internet && validated) "Online" else if (internet) "No internet" else "Local only"
        return NetworkState(type, status, internet && validated, kbps(c.linkDownstreamBandwidthKbps), kbps(c.linkUpstreamBandwidthKbps))
    }

    /** Android's own bandwidth estimate for the link, not a measured speed. */
    private fun kbps(k: Int): String = when {
        k <= 0 -> "Unavailable"
        k < 10_000 -> String.format(Locale.US, "est. %.1f Mbps", k / 1000f)
        else -> "est. ${k / 1000} Mbps"
    }
}
